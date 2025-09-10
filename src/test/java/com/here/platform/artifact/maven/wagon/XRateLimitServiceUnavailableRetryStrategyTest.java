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

import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;

import static com.here.platform.artifact.maven.wagon.XRateLimitServiceUnavailableRetryStrategy.RETRY_AFTER_HEADER;
import static com.here.platform.artifact.maven.wagon.XRateLimitServiceUnavailableRetryStrategy.X_RATE_LIMIT_RESET_HEADER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XRateLimitServiceUnavailableRetryStrategyTest {

  private ServiceUnavailableRetryStrategy strategy;

  private HttpResponse httpResponse;

  private HttpContext httpContext;

  @Before
  public void setup() {
    httpResponse = mock(HttpResponse.class, Answers.RETURNS_DEEP_STUBS);
    httpContext = mock(HttpContext.class, Answers.RETURNS_DEEP_STUBS);
    strategy = new XRateLimitServiceUnavailableRetryStrategy();
  }

  @Test
  public void testRetryTriggeredFor429ReponseCode() {
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(429);
    Assert.assertTrue(strategy.retryRequest(httpResponse, 1, httpContext));
  }

  @Test
  public void testRetryIntervalIsUsedFromXRateLimitResetHeader() {
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(429);
    when(httpResponse.getFirstHeader(X_RATE_LIMIT_RESET_HEADER).getValue()).thenReturn("99");
    when(httpResponse.containsHeader(X_RATE_LIMIT_RESET_HEADER)).thenReturn(true);
    strategy.retryRequest(httpResponse, 1, httpContext);
    Assert.assertEquals(99000, strategy.getRetryInterval());
  }

  @Test
  public void testRetryIntervalIsUsedFromXRateLimitResetHeaderWhenRetryAfterPresent() {
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(429);
    when(httpResponse.getFirstHeader(X_RATE_LIMIT_RESET_HEADER).getValue()).thenReturn("99");
    when(httpResponse.getFirstHeader(RETRY_AFTER_HEADER).getValue()).thenReturn("1");
    when(httpResponse.containsHeader(X_RATE_LIMIT_RESET_HEADER)).thenReturn(true);
    strategy.retryRequest(httpResponse, 1, httpContext);
    Assert.assertEquals(99000, strategy.getRetryInterval());
  }

  @Test
  public void testRetryIntervalIsUsedFromRetryAfter() {
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(429);
    when(httpResponse.getFirstHeader(RETRY_AFTER_HEADER).getValue()).thenReturn("1");
    when(httpResponse.containsHeader(X_RATE_LIMIT_RESET_HEADER)).thenReturn(false);
    strategy.retryRequest(httpResponse, 1, httpContext);
    Assert.assertEquals(1000, strategy.getRetryInterval());
  }

  @Test
  public void testDefaultRetryIntervalIsUsedWhenXRateLimitResetHeaderContainsNonNumericValue() {
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(429);
    when(httpResponse.getFirstHeader(X_RATE_LIMIT_RESET_HEADER).getValue()).thenReturn("asd");
    when(httpResponse.containsHeader(X_RATE_LIMIT_RESET_HEADER)).thenReturn(true);
    strategy.retryRequest(httpResponse, 1, httpContext);
    Assert.assertEquals(5000, strategy.getRetryInterval());
  }

  @Test
  public void testDefaultRetryIntervalIsUsedWhenExceptionOccurred() {
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(429);
    when(httpResponse.containsHeader(X_RATE_LIMIT_RESET_HEADER)).thenThrow(new RuntimeException());
    strategy.retryRequest(httpResponse, 1, httpContext);
    Assert.assertEquals(5000, strategy.getRetryInterval());
  }

}