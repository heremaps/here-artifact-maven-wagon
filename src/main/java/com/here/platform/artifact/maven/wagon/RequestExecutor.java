package com.here.platform.artifact.maven.wagon;

import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;

@FunctionalInterface
public interface RequestExecutor {

  CloseableHttpResponse apply(HttpUriRequest request) throws HttpException, IOException;

}
