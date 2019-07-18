package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.Rule;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

public class CreateCacheEntryFilter implements GatewayFilter {

  private static final Logger LOG = LoggerFactory.getLogger(CreateCacheEntryFilter.class);

  private final Store store;
  private final Rule createRule;

  public CreateCacheEntryFilter(final Store store,
                                final Rule createRule) {
    requireNonNull(store, "'store' must not be null!");
    requireNonNull(createRule, "'createRule' must not be null!");
    this.store = store;
    this.createRule = createRule;
  }

  @SuppressWarnings("WeakerAccess")
  protected boolean shouldCache(final ServerWebExchange exchange) {
    return createRule.applies(exchange);
  }

  @Override
  public Mono<Void> filter(final ServerWebExchange exchange,
                           final GatewayFilterChain chain) {

    if (!shouldCache(exchange)) {
      LOG.debug("Not caching exchange [{}].", exchange);
      return chain.filter(exchange);
    }

    return chain.filter(store.write(exchange));
  }
}
