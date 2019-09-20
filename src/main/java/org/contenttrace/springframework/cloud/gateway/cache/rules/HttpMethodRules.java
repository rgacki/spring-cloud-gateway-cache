package org.contenttrace.springframework.cloud.gateway.cache.rules;

import org.springframework.http.HttpMethod;

public final class HttpMethodRules {

  private static final Rule IS_GET_REQUEST = exchange -> exchange.getRequest().getMethod() == HttpMethod.GET;
  private static final Rule IS_HEAD_REQUEST = exchange -> exchange.getRequest().getMethod() == HttpMethod.HEAD;

  public static Rule isGetRequest() {
    return IS_GET_REQUEST;
  }

  public static Rule isHeadRequest() {
    return IS_HEAD_REQUEST;
  }

  private HttpMethodRules() {
    // void
  }
}
