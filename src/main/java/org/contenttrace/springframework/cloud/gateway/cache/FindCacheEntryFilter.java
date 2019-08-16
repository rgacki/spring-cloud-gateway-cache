package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.Rule;
import org.contenttrace.springframework.cloud.gateway.cache.store.Entry;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class FindCacheEntryFilter implements CacheEntryFilter, Ordered {

  private static final Logger LOG = LoggerFactory.getLogger(FindCacheEntryFilter.class);

  private final CacheConfiguration cacheConfiguration;
  private final Store store;
  private final Rule cacheRule;

  public FindCacheEntryFilter(final CacheConfiguration cacheConfiguration,
                              final Store store,
                              final Rule cacheRule) {
    this.cacheConfiguration = cacheConfiguration;
    this.store = store;
    this.cacheRule = cacheRule;
  }

  protected boolean canCache(final ServerWebExchange exchange) {
    return cacheRule.applies(exchange);
  }

  protected boolean isValid(final Entry entry) {
    // TODO: validate the entry.
    return true;
  }

  @Override
  public Mono<Void> filter(final ServerWebExchange exchange,
                           final GatewayFilterChain chain) {

    if (!canCache(exchange)) {
      LOG.debug("Exchange [{}] cannot be cached. Avoiding cache lookup.", exchange);
      return chain.filter(exchange);
    }

    store.find(exchange.getRequest()).ifPresent(entry -> {
      if (isValid(entry)) {
        if (cacheConfiguration.isExposeCacheEventHeader()) {
          exchange.getResponse().getHeaders().add(cacheConfiguration.getCacheEventHeaderName(), "hit");
        }
        exchange.getAttributes().put(CACHE_ENTRY_ATTRIBUTE_NAME, entry);
        // exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, cacheConfiguration.getOnValidCacheEntryRequestUrl());
        LOG.debug("Found cache entry [{}] for exchange [{}]. Request will be rerouted to [{}].", entry, exchange, cacheConfiguration.getOnValidCacheEntryRequestUrl());
      } else {
        entry.invalidate();
        LOG.debug("Found cache entry [{}] for exchange [{}], but is invalid.", entry, exchange);
      }
    });

    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE + 1;
  }

}
