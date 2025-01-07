/*
 * Copyright (C) 2018-2025 HERE Europe B.V.
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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.maven.wagon.shared.http.StandardServiceUnavailableRetryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * An implementation of the {@link ServiceUnavailableRetryStrategy} interface.
 * that retries {@code 408} (Request Timeout), {@code 429} (Too Many Requests),
 * and {@code 500} (Server side error) responses for a fixed number of times at
 * a interval returned in X-RateLimit-Reset or Retry-After header.
 * X-RateLimit-Reset have precedence over Retry-After.
 */
public class XRateLimitServiceUnavailableRetryStrategy extends StandardServiceUnavailableRetryStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(XRateLimitServiceUnavailableRetryStrategy.class);

  /**
   * The response HTTP header indicates how long the user agent should wait before making a follow-up request.
   * Custom header for Artifact Service. This should be same as the Retry-After header
   */
  public static final String X_RATE_LIMIT_RESET_HEADER = "X-RateLimit-Reset";

  /**
   * The response HTTP header indicates how long the user agent should wait before making a follow-up request.
   * Standard header for Artifact Service
   */
  public static final String RETRY_AFTER_HEADER = "Retry-After";

  /**
   * Maximum retries for requests that failed with {@code 408} (Request Timeout), {@code 429} (Too Many Requests),
   * and {@code 500} (Server side error) response code
   */
  private static final int MAX_RETRIES = 5;

  /**
   * Default delay between retries. Applied only if X-RateLimit-Reset and Retry-After headers are absent in response
   */
  private static final int DEFAULT_RETRY_INTERVAL_MS = 5000;

  private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

  private final ThreadLocal<HttpResponse> currentResponse = new ThreadLocal<>();

  public XRateLimitServiceUnavailableRetryStrategy() {
    super(MAX_RETRIES, DEFAULT_RETRY_INTERVAL_MS);
  }

  @Override
  public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
    this.currentResponse.set(response);
    return super.retryRequest(response, executionCount, context);
  }

  @Override
  public long getRetryInterval() {
    HttpResponse httpResponse = currentResponse.get();
    if (httpResponse != null) {
      try {
        Header waitHeader = httpResponse.containsHeader(X_RATE_LIMIT_RESET_HEADER) ?
            httpResponse.getFirstHeader(X_RATE_LIMIT_RESET_HEADER)
            : httpResponse.getFirstHeader(RETRY_AFTER_HEADER);
        if (waitHeader != null) {
          String value = waitHeader.getValue();
          if(value != null && DIGIT_PATTERN.matcher(value).matches()) {
            LOG.info("Request is failed with code {}. Retrying in {} seconds", httpResponse.getStatusLine().getStatusCode(), value);
            return Long.parseLong(value) * 1000;
          } else {
            LOG.warn("Header {} have value {} but numeric value expected", waitHeader.getName(), waitHeader.getValue() );
          }
        }
      } catch (Exception e) {
        LOG.warn("Unexpected exception occurred. Fallback to standard retry logic", e);
      }
    }
    return super.getRetryInterval();
  }

}
