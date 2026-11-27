/*
 * Copyright (C) 2018-2026 HERE Europe B.V.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.here.account.auth.JwtClientAssertionProvider;
import com.here.account.auth.OAuth1ClientCredentialsProvider;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ArtifactWagonAuthenticationTest {

  private static final String CLIENT_ID = "test-client-id";
  private static final String KEY_ID = "test-key-id";
  private static final String ACCESS_TOKEN = "test-access-token";
  private static final String JWT_BEARER_ASSERTION_TYPE =
      "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

  private static KeyPair keyPair;

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final List<TokenRequest> tokenRequests = new CopyOnWriteArrayList<>();
  private HttpServer tokenServer;
  private String tokenEndpointUrl;

  @BeforeClass
  public static void generateKeyPair() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    keyPair = generator.generateKeyPair();
  }

  @Before
  public void startTokenServer() throws IOException {
    tokenServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    tokenServer.createContext(
        "/oauth2/token",
        exchange -> {
          String body =
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          tokenRequests.add(
              new TokenRequest(
                  exchange.getRequestHeaders().getFirst("Authorization"), parseForm(body)));
          byte[] response =
              String.format(
                      "{\"access_token\":\"%s\",\"token_type\":\"bearer\",\"expires_in\":3600}",
                      ACCESS_TOKEN)
                  .getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, response.length);
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(response);
          }
        });
    tokenServer.start();
    InetSocketAddress address = tokenServer.getAddress();
    tokenEndpointUrl =
        String.format(
            "http://%s:%d/oauth2/token", address.getHostString(), address.getPort());
  }

  @After
  public void stopTokenServer() {
    tokenServer.stop(0);
  }

  @Test
  public void testPrivateKeyJwtProviderSelected() {
    Properties properties = privateKeyJwtProperties(inlinePrivateKey());

    assertTrue(
        ArtifactWagon.createClientAuthorizationRequestProvider(properties)
            instanceof JwtClientAssertionProvider);
  }

  @Test
  public void testOAuth1ProviderSelectedByDefault() {
    assertTrue(
        ArtifactWagon.createClientAuthorizationRequestProvider(oauth1Properties())
            instanceof OAuth1ClientCredentialsProvider);
  }

  @Test
  public void testPrivateKeyJwtWithoutPrivateKeyFails() {
    Properties properties = privateKeyJwtProperties(inlinePrivateKey());
    properties.remove("here.private.key");

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ArtifactWagon.createClientAuthorizationRequestProvider(properties));
    assertTrue(exception.getMessage().contains("here.private.key"));
  }

  @Test
  public void testPrivateKeyJwtWithInvalidKeyFailsAuthentication() throws Exception {
    ArtifactWagon wagon = wagonWith(privateKeyJwtProperties("MIIinvalid"));
    HttpGet request = new HttpGet("http://localhost/");

    assertThrows(HereAuthenticationException.class, () -> wagon.setHeaders(request));
    assertTrue(tokenRequests.isEmpty());
  }

  @Test
  public void testMissingTokenEndpointUrlReported() throws Exception {
    Properties properties = oauth1Properties();
    properties.remove("here.token.endpoint.url");
    ArtifactWagon wagon = wagonWith(properties);

    HereAuthenticationException exception =
        assertThrows(
            HereAuthenticationException.class, () -> wagon.setHeaders(new HttpGet("http://localhost/")));
    assertTrue(exception.getCause().getMessage().contains("here.token.endpoint.url"));
    exception = assertThrows(HereAuthenticationException.class, wagon::getDefaultArtifactServiceUrl);
    assertTrue(exception.getMessage().contains("here.token.endpoint.url"));
    assertTrue(tokenRequests.isEmpty());
  }

  @Test
  public void testBearerTokenMintedWithPrivateKeyJwtFromKeyFile() throws Exception {
    File keyFile = temporaryFolder.newFile("private-key.pem");
    Files.write(keyFile.toPath(), pemPrivateKey().getBytes(StandardCharsets.US_ASCII));

    assertBearerTokenMintedWithPrivateKeyJwt(
        privateKeyJwtProperties(keyFile.getAbsolutePath()));
  }

  @Test
  public void testBearerTokenMintedWithPrivateKeyJwtFromCredentialsString() throws Exception {
    // same shape as the HERE_CREDENTIALS_STRING environment variable
    String credentialsString =
        String.join(
            "\n",
            "here.token.endpoint.url=" + tokenEndpointUrl,
            "here.auth.method=private_key_jwt",
            "here.client.id=" + CLIENT_ID,
            "here.key.id=" + KEY_ID,
            "here.private.key=" + inlinePrivateKey());
    Properties properties = new Properties();
    properties.load(new StringReader(credentialsString));

    assertBearerTokenMintedWithPrivateKeyJwt(properties);
  }

  @Test
  public void testBearerTokenMintedWithOAuth1() throws Exception {
    HttpGet request = new HttpGet("http://localhost/");
    wagonWith(oauth1Properties()).setHeaders(request);

    assertEquals("Bearer " + ACCESS_TOKEN, request.getFirstHeader("Authorization").getValue());
    assertEquals(1, tokenRequests.size());
    TokenRequest tokenRequest = tokenRequests.get(0);
    assertNotNull(tokenRequest.authorization);
    assertTrue(tokenRequest.authorization.startsWith("OAuth "));
    assertEquals("client_credentials", tokenRequest.form.get("grant_type"));
    assertFalse(tokenRequest.form.containsKey("client_assertion"));
  }

  private void assertBearerTokenMintedWithPrivateKeyJwt(Properties properties) throws Exception {
    HttpGet request = new HttpGet("http://localhost/");
    wagonWith(properties).setHeaders(request);

    assertEquals("Bearer " + ACCESS_TOKEN, request.getFirstHeader("Authorization").getValue());
    assertEquals(1, tokenRequests.size());
    TokenRequest tokenRequest = tokenRequests.get(0);
    assertNull(tokenRequest.authorization);
    assertEquals("client_credentials", tokenRequest.form.get("grant_type"));
    assertEquals(JWT_BEARER_ASSERTION_TYPE, tokenRequest.form.get("client_assertion_type"));

    String[] jwt = tokenRequest.form.get("client_assertion").split("\\.");
    assertEquals(3, jwt.length);
    assertTrue(isSignatureValid(jwt));

    JsonNode header = decodeJwtPart(jwt[0]);
    assertEquals("RS256", header.get("alg").asText());
    assertEquals(KEY_ID, header.get("kid").asText());

    JsonNode claims = decodeJwtPart(jwt[1]);
    assertEquals(CLIENT_ID, claims.get("iss").asText());
    assertEquals(CLIENT_ID, claims.get("sub").asText());
    assertEquals(tokenEndpointUrl, claims.get("aud").asText());
  }

  private Properties privateKeyJwtProperties(String privateKey) {
    Properties properties = new Properties();
    properties.setProperty("here.token.endpoint.url", tokenEndpointUrl);
    properties.setProperty("here.auth.method", "private_key_jwt");
    properties.setProperty("here.client.id", CLIENT_ID);
    properties.setProperty("here.key.id", KEY_ID);
    properties.setProperty("here.private.key", privateKey);
    return properties;
  }

  private Properties oauth1Properties() {
    Properties properties = new Properties();
    properties.setProperty("here.token.endpoint.url", tokenEndpointUrl);
    properties.setProperty("here.client.id", CLIENT_ID);
    properties.setProperty("here.access.key.id", "test-access-key-id");
    properties.setProperty("here.access.key.secret", "test-access-key-secret");
    return properties;
  }

  private static ArtifactWagon wagonWith(Properties properties) throws IllegalAccessException {
    return new ArtifactWagon() {
      @Override
      protected Properties loadHereProperties() {
        return properties;
      }
    };
  }

  private static String inlinePrivateKey() {
    return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
  }

  private static String pemPrivateKey() {
    return "-----BEGIN PRIVATE KEY-----\n"
        + Base64.getMimeEncoder(64, new byte[] {'\n'})
            .encodeToString(keyPair.getPrivate().getEncoded())
        + "\n-----END PRIVATE KEY-----\n";
  }

  private static boolean isSignatureValid(String[] jwt) throws Exception {
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initVerify(keyPair.getPublic());
    signature.update((jwt[0] + "." + jwt[1]).getBytes(StandardCharsets.US_ASCII));
    return signature.verify(Base64.getUrlDecoder().decode(jwt[2]));
  }

  private JsonNode decodeJwtPart(String part) throws IOException {
    return objectMapper.readTree(Base64.getUrlDecoder().decode(part));
  }

  private static Map<String, String> parseForm(String body) {
    Map<String, String> form = new HashMap<>();
    for (String pair : body.split("&")) {
      String[] keyValue = pair.split("=", 2);
      form.put(
          URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
          keyValue.length > 1 ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "");
    }
    return form;
  }

  private static final class TokenRequest {
    private final String authorization;
    private final Map<String, String> form;

    private TokenRequest(String authorization, Map<String, String> form) {
      this.authorization = authorization;
      this.form = form;
    }
  }
}
