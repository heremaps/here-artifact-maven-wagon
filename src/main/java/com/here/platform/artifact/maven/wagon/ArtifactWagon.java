/*
 * Copyright (C) 2018-2020 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */
package com.here.platform.artifact.maven.wagon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;
import org.apache.maven.wagon.shared.http.AbstractHttpClientWagon;
import org.apache.maven.wagon.shared.http.EncodingUtil;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.here.account.auth.OAuth1ClientCredentialsProvider;
import com.here.account.http.HttpConstants;
import com.here.account.http.HttpProvider;
import com.here.account.http.apache.ApacheHttpClientProvider;
import com.here.account.oauth2.ClientAuthorizationRequestProvider;
import com.here.account.oauth2.ClientCredentialsGrantRequest;
import com.here.account.oauth2.HereAccount;
import com.here.account.oauth2.TokenEndpoint;
import com.here.platform.artifact.maven.wagon.model.RegisterRequest;
import com.here.platform.artifact.maven.wagon.model.RegisterResponse;
import com.here.platform.artifact.maven.wagon.model.ServiceExceptionResponse;
import com.here.platform.artifact.maven.wagon.resolver.ArtifactWagonPropertiesResolver;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Wagon provider with two main responsibilities:
 *
 * <ul>
 *   <li>Minting a HERE Auth token before each request and forwarding that token as a bearer
 *       credential
 *   <li>Encoding the URL into HERE HRN format on the server-end
 * </ul>
 */
@Component(role = Wagon.class)
public class ArtifactWagon extends AbstractHttpClientWagon {

  private static final String AUTHORIZATION_FORBIDDEN_ERROR_MESSAGE =
      "The resource may already exist "
          + "in the system but your credentials may not grant modification privileges to it";

  private static final Logger LOG = LoggerFactory.getLogger(ArtifactWagon.class);
  private static final String REGISTER_PREFIX = "register";
  private static final String HERE_CREDENTIALS_PROPERTY = "hereCredentialsFile";
  private static final String HERE_CREDENTIALS_ENV = "HERE_CREDENTIALS_FILE";
  private static final String HERE_CREDENTIALS_PATH = ".here/credentials.properties";
  private static final String HERE_ENDPOINT_URL_KEY = "here.token.endpoint.url";
  private static final String HERE_ACCESS_SECRET_KEY = "here.access.key.secret";
  private static final String HERE_ACCESS_ID_KEY = "here.access.key.id";
  private static final String HERE_USER_ID_KEY = "here.user.id";

  /**
   * Defines the protocol mapping to use. NOTE: The order of the mapping becomes the search order.
   */
  private static final String[][] PROTOCOL_MAP =
      new String[][] {
        {"here+http://", "http://"}, // http and here auth
        {"here+https://", "https://"}, // https and here auth
      };

  private static final String ARTIFACT_SERVICE_URL_PLACEHOLDER_PROTOCOL = "here+artifact-service";

  private final Object lock = new Object();
  private final ObjectMapper objectMapper;
  private final String defaultArtifactServiceUrl;
  private final Properties hereProperties;

  private String authorization;

  public ArtifactWagon() {
    // load the HERE credentials file
    this.hereProperties = loadHereProperties();

    // resolve hrnPrefix and artifactServiceUrl by here token endpoint url.
    String hereTokenEndpointUrl = this.hereProperties.getProperty(HERE_ENDPOINT_URL_KEY);
    ArtifactWagonPropertiesResolver environmentPropertiesResolver =
        new ArtifactWagonPropertiesResolver();
    this.defaultArtifactServiceUrl =
        environmentPropertiesResolver.resolveArtifactServiceUrl(hereTokenEndpointUrl);

    // configure JSON
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  }

  @Override
  public void setHeaders(HttpUriRequest method) {
    synchronized (lock) {
      // just authorize once
      if (authorization == null) {
        Properties properties = this.hereProperties;

        // override with security information
        if (getAuthenticationInfo() != null) {
          properties = new Properties();
          properties.putAll(this.hereProperties);

          if (isNotEmpty(getAuthenticationInfo().getUserName())) {
            properties.setProperty(HERE_ACCESS_ID_KEY, getAuthenticationInfo().getUserName());
          }
          if (isNotEmpty(getAuthenticationInfo().getPassword())) {
            properties.setProperty(HERE_ACCESS_SECRET_KEY, getAuthenticationInfo().getPassword());
          }
        }

        // attempt to make an authorization
        authorization = mintAuthorizationToken(properties);
        LOG.trace("Obtained bearer token: {}", authorization);
      }
    }

    Properties properties = super.getHttpHeaders();
    if (properties == null) {
      properties = new Properties();
    }
    properties.setProperty("Authorization", String.format("Bearer %s", authorization));
    setHttpHeaders(properties);

    super.setHeaders(method);
  }

  @Override
  public String getURL(Repository repository) {
    return resolveRepositoryUrl(repository.getUrl());
  }

  private String resolveRepositoryUrl(String url) {
    // return as-is if no mapping should be done
    String resolvedUrl = url;

    // Process protocol mappings
    if (url.startsWith(ARTIFACT_SERVICE_URL_PLACEHOLDER_PROTOCOL)) {
      resolvedUrl = defaultArtifactServiceUrl;
    } else {
      // normalize url protocol
      for (String[] entry : PROTOCOL_MAP) {
        String protocol = entry[0];
        if (url.startsWith(protocol)) {
          resolvedUrl = entry[1] + url.substring(protocol.length());
        }
      }
    }
    return resolvedUrl;
  }

  @Override
  protected ProxyInfo getProxyInfo(String protocol, String host) {
    ProxyInfo proxyInfo = super.getProxyInfo(protocol, host);

    // Handle situation when wagon is instantiated in a plugin and does not return the correct proxy
    if (proxyInfo == null && !protocol.startsWith("here+")) {
      proxyInfo =
          Optional.ofNullable(super.getProxyInfo("here+" + protocol, host))
              .orElseGet(() -> super.getProxyInfo(ARTIFACT_SERVICE_URL_PLACEHOLDER_PROTOCOL, host));
    }
    return proxyInfo;
  }

  @Override
  protected InputStream getInputStream(Resource resource)
      throws TransferFailedException, ResourceDoesNotExistException {
    // update to the correct path
    String path = verifyAndRewrite(resource.getName());
    resource.setName(path);

    try {
      return super.getInputStream(resource);
    } catch (AuthorizationException exp) {
      throw new ResourceDoesNotExistException(
          "Authorization error using path: " + resource.getName(), exp);
    }
  }

  @Override
  public void put(File source, String resourceName)
      throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
    resourceName = registerAndRewrite(resourceName);
    try {
      super.put(source, resourceName);
    } catch (AuthorizationException e) {
      throw new AuthorizationException(AUTHORIZATION_FORBIDDEN_ERROR_MESSAGE, e);
    }
  }

  @Override
  public void putFromStream(
      InputStream stream, String destination, long contentLength, long lastModified)
      throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
    destination = registerAndRewrite(destination);
    try {
      super.putFromStream(stream, destination, contentLength, lastModified);
    } catch (AuthorizationException e) {
      throw new AuthorizationException(AUTHORIZATION_FORBIDDEN_ERROR_MESSAGE, e);
    }
  }

  /**
   * Register the URL in artifact
   *
   * @param destination
   * @return
   * @throws TransferFailedException
   */
  private String registerAndRewrite(String destination) throws TransferFailedException {
    try {
      Artifact artifact = toArtifact(destination);
      RegisterResponse registerResponse = registerArtifact(artifact);

      destination = buildPath(registerResponse.getGroupHrnPrefix(), artifact);
      LOG.trace("Rewrote path for put: {}", destination);
    } catch (InvalidPathException exp) {
      LOG.warn(String.format("Invalid path passed into wagon provider: %s", destination), exp);
    }

    return destination;
  }

  /**
   * Register the URL in artifact
   *
   * @param destination
   * @return
   * @throws TransferFailedException
   */
  private String verifyAndRewrite(String destination)
      throws TransferFailedException, ResourceDoesNotExistException {
    try {
      Artifact artifact = toArtifact(destination);
      RegisterResponse registerResponse = registerExists(artifact);

      destination = buildPath(registerResponse.getGroupHrnPrefix(), artifact);
      LOG.trace("Rewrote path for put: {}", destination);
    } catch (InvalidPathException exp) {
      LOG.warn(String.format("Invalid path passed into wagon provider: %s", destination), exp);
      throw new ResourceDoesNotExistException(destination);
    }

    return destination;
  }

  /**
   * Register the prefix
   *
   * @param artifact
   * @throws TransferFailedException
   */
  private RegisterResponse registerArtifact(Artifact artifact) throws TransferFailedException {
    String registerPath =
        String.format("%s/%s/%s", REGISTER_PREFIX, artifact.getGroupId(), artifact.getArtifactId());
    try {
      String url = EncodingUtil.encodeURLToString(getURL(getRepository()), registerPath);
      HttpPut httpPut = new HttpPut(url);
      httpPut.addHeader("Content-Type", "application/json");

      // add in user identifier to request
      RegisterRequest request = new RegisterRequest(hereProperties.getProperty(HERE_USER_ID_KEY));
      httpPut.setEntity(new ByteArrayEntity(objectMapper.writeValueAsBytes(request)));
      CloseableHttpResponse httpResponse = execute(httpPut);
      HttpEntity httpEntity = httpResponse.getEntity();
      try {
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK && status != HttpStatus.SC_CREATED) {
          String message = "";
          String content = EntityUtils.toString(httpResponse.getEntity());
          if (!content.isEmpty()) {
            ServiceExceptionResponse exceptionResponse =
                objectMapper.readValue(content, ServiceExceptionResponse.class);
            message = exceptionResponse.getMessage();
          }
          throw new TransferFailedException(
              String.format(
                  "Unable to register group %s and artifact %s: %s %s",
                  artifact.getGroupId(),
                  artifact.getArtifactId(),
                  httpResponse.getStatusLine(),
                  message));
        }
        InputStream content = httpEntity.getContent();
        return objectMapper.readValue(content, RegisterResponse.class);
      } finally {
        consumeQuietly(httpResponse);
      }
    } catch (TransferFailedException exp) {
      LOG.error(
          "Error during registerArtifact using path '{}': {}", registerPath, exp.getMessage());
      throw exp;
    } catch (Exception exp) {
      String msg =
          String.format(
              "Error during registerArtifact using path '%s': %s", registerPath, exp.getMessage());
      throw new TransferFailedException(msg, exp);
    }
  }

  /**
   * Register the prefix
   *
   * @param artifact
   * @throws ResourceDoesNotExistException
   */
  private RegisterResponse registerExists(Artifact artifact)
      throws ResourceDoesNotExistException, TransferFailedException {
    String registerPath =
        String.format("%s/%s/%s", REGISTER_PREFIX, artifact.getGroupId(), artifact.getArtifactId());
    String url = EncodingUtil.encodeURLToString(getURL(getRepository()), registerPath);
    HttpGet httpGet = new HttpGet(url);

    try {
      // add in user identifier to request
      CloseableHttpResponse httpResponse = execute(httpGet);
      try {
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
          throw new ResourceDoesNotExistException(registerPath);
        }
        InputStream content = httpResponse.getEntity().getContent();
        return objectMapper.readValue(content, RegisterResponse.class);
      } finally {
        consumeQuietly(httpResponse);
      }
    } catch (IOException | HttpException exp) {
      String msg =
          String.format(
              "Error during registerExists using path '%s': %s", registerPath, exp.getMessage());
      throw new TransferFailedException(msg, exp);
    }
  }

  private Artifact toArtifact(String url) {
    String[] parts = url.split("/");
    if (parts.length != 4) {
      throw new InvalidPathException(url);
    }

    return new Artifact(parts[0], parts[1], parts[2], parts[3]);
  }

  private String buildPath(String groupHrnPrefix, Artifact artifact) {
    // magic string to indicate root directory so all paths have version (easier to pattern match)
    String version = defaultIfEmpty(artifact.getVersion(), "NONE");
    return String.format(
        "%s:%s:%s/%s", groupHrnPrefix, artifact.getArtifactId(), version, artifact.getFile());
  }

  private String mintAuthorizationToken(Properties properties) {
    LOG.trace("Attempting to authenticate with HERE Account");

    try {
      String endpointUrl = properties.getProperty(HERE_ENDPOINT_URL_KEY);
      if (endpointUrl == null) {
        throw new IllegalArgumentException(
            String.format("No %s property specified", HERE_ENDPOINT_URL_KEY));
      }
      ClientAuthorizationRequestProvider credentialsProvider =
          new OAuth1ClientCredentialsProvider.FromProperties(properties);
      HttpProvider httpProvider = createHttpProvider(endpointUrl);
      TokenEndpoint tokenEndpoint = HereAccount.getTokenEndpoint(httpProvider, credentialsProvider);
      return tokenEndpoint.requestToken(new ClientCredentialsGrantRequest()).getAccessToken();
    } catch (Exception exp) {
      throw new HereAuthenticationException("Error authenticating HERE credentials", exp);
    }
  }

  /**
   * Load the HERE credentials file
   *
   * @return the loaded properties
   */
  protected Properties loadHereProperties() {
    Properties properties = new Properties();

    // check for credentials file
    File file;
    String systemPropertyFile = System.getProperty(HERE_CREDENTIALS_PROPERTY);
    if (isNotEmpty(systemPropertyFile)) {
      LOG.debug(
          "Found property file value at System Property {}: {}",
          HERE_CREDENTIALS_PROPERTY,
          systemPropertyFile);
    } else {
      systemPropertyFile = System.getenv(HERE_CREDENTIALS_ENV);
      if (isNotEmpty(systemPropertyFile)) {
        LOG.debug(
            "Found property file at Environment Property {}: {}",
            HERE_CREDENTIALS_ENV,
            systemPropertyFile);
      }
    }

    if (isNotEmpty(systemPropertyFile)) {
      file = new File(systemPropertyFile);
    } else {
      file = new File(System.getProperty("user.home"), HERE_CREDENTIALS_PATH);
    }

    LOG.debug("Using here credentials file: {}", file.getAbsolutePath());
    if (file.exists() && file.canRead()) {
      LOG.debug("Attempting to read credentials file at: {}", file.getAbsolutePath());
      try (InputStream in = new FileInputStream(file)) {
        properties.load(in);
      } catch (IOException exp) {
        LOG.warn("Unable to read client credentials at {}", file.getAbsolutePath(), exp);
      }
    } else {
      LOG.warn("Unable to read configured file: {}", file.getAbsolutePath());
    }

    return properties;
  }

  /**
   * Create the HttpProvider to use for HereAccount. This must add in any proxy settings that maven
   * is aware of and forward them into the underlying http client
   *
   * @param endpointUrl
   * @return
   */
  private HttpProvider createHttpProvider(final String endpointUrl) {
    // default configuration
    RequestConfig.Builder requestConfigBuilder =
        RequestConfig.custom()
            .setConnectTimeout(HttpConstants.DEFAULT_CONNECTION_TIMEOUT_IN_MS)
            .setConnectionRequestTimeout(HttpConstants.DEFAULT_REQUEST_TIMEOUT_IN_MS);

    // push in proxy information to the underlying http client
    CredentialsProvider credentialsProvider = null;
    URI endpointUri = URI.create(endpointUrl);
    ProxyInfo proxyInfo = getProxyInfo(endpointUri.getScheme(), endpointUri.getHost());
    if (proxyInfo != null) {
      LOG.debug("Found proxy information: {}:{}", proxyInfo.getHost(), proxyInfo.getPort());
      requestConfigBuilder.setProxy(new HttpHost(proxyInfo.getHost(), proxyInfo.getPort()));
      if (isNotEmpty(proxyInfo.getUserName())) {
        LOG.debug("Found proxy security: {}", proxyInfo.getUserName());
        credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            new AuthScope(proxyInfo.getHost(), proxyInfo.getPort()),
            new UsernamePasswordCredentials(proxyInfo.getUserName(), proxyInfo.getPassword()));
      }
    }

    HttpClientBuilder clientBuilder =
        HttpClientBuilder.create().setDefaultRequestConfig(requestConfigBuilder.build());
    if (credentialsProvider != null) {
      clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }

    return ApacheHttpClientProvider.builder()
        .setHttpClient(clientBuilder.build())
        .setDoCloseHttpClient(true)
        .build();
  }

  private void consumeQuietly(CloseableHttpResponse httpResponse) {
    if (httpResponse != null) {
      EntityUtils.consumeQuietly(httpResponse.getEntity());
      try {
        httpResponse.close();
      } catch (IOException exp) {
        LOG.trace("Error during consuming response", exp);
      }
    }
  }
}
