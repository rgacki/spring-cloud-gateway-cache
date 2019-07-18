package org.contenttrace.springframework.cloud.gateway.cache.rules;

import org.springframework.web.server.ServerWebExchange;

public final class WildcardVaryRule implements Rule {

  @Override
  public boolean applies(final ServerWebExchange exchange) {
    return exchange.getRequest().getHeaders().getVary().stream().anyMatch("*"::equals);
  }
}
