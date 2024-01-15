/*
 * Copyright (C) 2018-2024 HERE Europe B.V.
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
package com.here.platform.artifact.maven.wagon.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

/**
 * Return Artifact Service URL cached in temporary file
 */
class ArtifactServiceUrlFileResolver implements ArtifactServiceUrlResolver {

  private static final Logger LOG = LoggerFactory.getLogger(ArtifactServiceUrlFileResolver.class);

  private static final String FILE_NAME = "artifact_wagon_url_cache.properties";

  private static final long MAX_FILE_AGE_MINUTES = 5;

  @Override
  public String resolveArtifactServiceUrl(String tokenUrl) {
    try {
      Properties prop = loadCachedProperties(getCacheFile());
      return prop.getOrDefault(tokenUrl, "").toString();
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Can't resolve Artifact Service URL from file", e);
      } else {
        LOG.warn("Can't resolve Artifact Service URL from file");
      }
      return "";
    }
  }

  @Override
  public void afterUrlResolved(String tokenUrl, String resolvedUrl) {
    try {
      Path cacheFile = getCacheFile();
      Properties prop = loadCachedProperties(cacheFile);
      try (OutputStream output = Files.newOutputStream(cacheFile)) {
        prop.put(tokenUrl, resolvedUrl);
        prop.store(output, "Artifact Service URL cache");
      }
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Failed to update Artifact Service URL in temp file", e);
      }
    }
  }

  private Properties loadCachedProperties(Path path) throws IOException {
    try (InputStream input = Files.newInputStream(path)) {
      Properties prop = new Properties();
      prop.load(input);
      return prop;
    }
  }

  private Path getCacheFile() throws IOException {
    Path path = Paths.get(System.getProperty("java.io.tmpdir"), FILE_NAME);
    File file = path.toFile();
    if (!file.createNewFile()) {
      if (isFileOlderThan(path, MAX_FILE_AGE_MINUTES)) {
        file.delete();
        file.createNewFile();
      }
    }
    return path;
  }

  private boolean isFileOlderThan(Path path, long minutes) throws IOException {
    Instant fileInstant = Files.readAttributes(path, BasicFileAttributes.class).creationTime().toInstant();
    Instant now = Instant.now();
    Duration difference = Duration.between(fileInstant, now);
    return difference.toMinutes() > minutes;
  }

}
