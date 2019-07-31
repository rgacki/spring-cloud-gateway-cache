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

  private final CacheConfiguration cacheConfiguration;
  private final Store store;
  private final Rule createRule;

  public CreateCacheEntryFilter(final CacheConfiguration cacheConfiguration,
                                final Store store,
                                final Rule createRule) {
    requireNonNull(cacheConfiguration, "'cacheConfiguration' must not be null!");
    requireNonNull(store, "'store' must not be null!");
    requireNonNull(createRule, "'createRule' must not be null!");
    this.cacheConfiguration = cacheConfiguration;
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

    if (cacheConfiguration.isExposeCacheEventHeader()) {
      exchange.getResponse().getHeaders().add("X-Cache-Event", "store");
    }

    return chain.filter(store.write(exchange));
  }
}
