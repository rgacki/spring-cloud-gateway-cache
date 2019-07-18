package org.contenttrace.springframework.cloud.gateway.cache.rules;

import org.contenttrace.springframework.cloud.gateway.cache.CacheControl;

public class StandardRules {

  public static Rule httpCompliant() {
    return Rule.and(
      Rule.or(HttpMethodRules.isGetRequest(), HttpMethodRules.isHeadRequest()),
      Rule.not(CacheControlRules.responseHasNoCacheEnabled()),
      Rule.not(CacheControlRules.responseHasNoStoreEnabled()),
      Rule.not(CacheControlRules.responseIsPrivate()),
      Rule.not(new WildcardVaryRule()),
      Rule.not(exchange -> exchange.getResponse().getHeaders().containsKey("Set-Cookie"))
    );
  }


  private StandardRules() {
    // void
  }
}
