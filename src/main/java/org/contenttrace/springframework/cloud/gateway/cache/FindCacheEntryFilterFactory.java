package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.Rule;
import org.contenttrace.springframework.cloud.gateway.cache.store.Entry;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;

public class FindCacheEntryFilterFactory extends AbstractGatewayFilterFactory<FindCacheEntryFilterFactory.Config>
  implements CacheEntryFilterFactory<FindCacheEntryFilterFactory.Config> {

  private static final Logger LOG = LoggerFactory.getLogger(FindCacheEntryFilterFactory.class);

  private final CacheConfiguration cacheConfiguration;
  private final Store store;
  private final Rule rule;

  public FindCacheEntryFilterFactory(CacheConfiguration cacheConfiguration, Store store, Rule rule) {
    this.cacheConfiguration = cacheConfiguration;
    this.store = store;
    this.rule = rule;
  }

  protected boolean canCache(final ServerWebExchange exchange) {
    return rule.applies(exchange);
  }

  protected boolean isValid(final Entry entry) {
    // TODO: validate the entry.
    return true;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new OrderedGatewayFilter(((exchange, chain) -> {

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
    }), OrderedGatewayFilter.HIGHEST_PRECEDENCE + 1);
  }

  public static class Config {

  }
}
