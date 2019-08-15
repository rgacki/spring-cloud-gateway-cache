package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.Rule;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.WebClientWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

public class CreateCacheEntryFilter implements CacheEntryFilter, Ordered {

  private static final Logger LOG = LoggerFactory.getLogger(CreateCacheEntryFilter.class);

  private final CacheConfiguration cacheConfiguration;
  private final Store store;
  private final Rule cacheRule;

  public CreateCacheEntryFilter(final CacheConfiguration cacheConfiguration,
                                final Store store,
                                final Rule cacheRule) {
    requireNonNull(cacheConfiguration, "'cacheConfiguration' must not be null!");
    requireNonNull(store, "'store' must not be null!");
    requireNonNull(cacheRule, "'createRule' must not be null!");
    this.cacheConfiguration = cacheConfiguration;
    this.store = store;
    this.cacheRule = cacheRule;
  }

  @SuppressWarnings("WeakerAccess")
  protected boolean shouldCache(final ServerWebExchange exchange) {
    return cacheRule.applies(exchange);
  }

  @Override
  public Mono<Void> filter(final ServerWebExchange exchange,
                           final GatewayFilterChain chain) {

    if (!shouldCache(exchange)) {
      LOG.debug("Not caching exchange [{}].", exchange);
      return chain.filter(exchange);
    }

    if (cacheConfiguration.isExposeCacheEventHeader()) {
      exchange.getResponse().getHeaders().add(cacheConfiguration.getCacheEventHeaderName(), "store");
    }

    return chain.filter(store.write(exchange));
  }

  @Override
  public int getOrder() {
    return WebClientWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
  }
}
