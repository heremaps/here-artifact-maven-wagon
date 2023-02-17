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
package com.here.platform.artifact.maven.wagon.layout;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.spi.connector.layout.RepositoryLayout;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import static com.here.platform.artifact.maven.wagon.util.StringUtils.defaultIfEmpty;
import static com.here.platform.artifact.maven.wagon.util.StringUtils.isEmpty;

/**
 * Based on {@link org.eclipse.aether.internal.impl.Maven2RepositoryLayoutFactory} Maven2RepositoryLayout.
 *
 * <p>This layout attempts to make URLs that look like: {groupId}/{artifact}/{version}/{file} for
 * easy translation into HRN format within the wagon provider.
 */
public class HereRepositoryLayoutDecorator implements java.lang.reflect.InvocationHandler {

  private static final char PATH_SEPARATOR = '/';
  private static final char ARTIFACT_SEPARATOR = '-';
  private static final char EXTENSION_SEPARATOR = '.';
  private static final int LOCATION_CAPACITY = 128;
  private static final Method getLocation_artifact_upload_method;
  private static final Method getLocation_metadata_upload_method;

  static {
    try {
      getLocation_artifact_upload_method = RepositoryLayout.class.getMethod("getLocation", Artifact.class, boolean.class);
      getLocation_metadata_upload_method = RepositoryLayout.class.getMethod("getLocation", Metadata.class, boolean.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private final RepositoryLayout delegate;

  public static Object newInstance(RepositoryLayout delegate) {
    return java.lang.reflect.Proxy.newProxyInstance(
        delegate.getClass().getClassLoader(),
        new Class[] { RepositoryLayout.class },
        new HereRepositoryLayoutDecorator(delegate));
  }

  private HereRepositoryLayoutDecorator(RepositoryLayout delegate) {
    this.delegate = delegate;
  }

  public URI getLocation(Artifact artifact, boolean upload) {
    StringBuilder path = new StringBuilder(LOCATION_CAPACITY);

    // build segments
    path.append(artifact.getGroupId()).append(PATH_SEPARATOR);
    path.append(artifact.getArtifactId()).append(PATH_SEPARATOR);
    path.append(artifact.getBaseVersion()).append(PATH_SEPARATOR);

    path.append(artifact.getArtifactId()).append(ARTIFACT_SEPARATOR).append(artifact.getVersion());
    if (!isEmpty(artifact.getClassifier())) {
      path.append(ARTIFACT_SEPARATOR).append(artifact.getClassifier());
    }

    if (!isEmpty(artifact.getExtension())) {
      path.append(EXTENSION_SEPARATOR).append(artifact.getExtension());
    }

    return toUri(path.toString());
  }

  public URI getLocation(Metadata metadata, boolean upload) {
    if (metadata.getGroupId().isEmpty() || metadata.getArtifactId().isEmpty()) {
      throw new IllegalArgumentException("Invalid path for HERE layout: " + metadata);
    }

    StringBuilder path = new StringBuilder(LOCATION_CAPACITY);
    path.append(metadata.getGroupId()).append(PATH_SEPARATOR);
    path.append(metadata.getArtifactId()).append(PATH_SEPARATOR);
    path.append(defaultIfEmpty(metadata.getVersion(), "NONE")).append(PATH_SEPARATOR);
    path.append(metadata.getType());

    return toUri(path.toString());
  }

  private URI toUri(String path) {
    try {
      return new URI(null, null, path, null);
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
    if (m.equals(getLocation_artifact_upload_method) && args.length == 2) {
      return getLocation((Artifact) args[0], (boolean) args[1]);
    } else if (m.equals(getLocation_metadata_upload_method) && args.length == 2) {
      return getLocation((Metadata) args[0], (boolean) args[1]);
    } else {
      return m.invoke(delegate, args);
    }
  }

}
