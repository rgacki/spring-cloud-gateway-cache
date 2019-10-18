package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.Rule;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.WebClientWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;

public class CreateCacheEntryFilterFactory extends AbstractGatewayFilterFactory<CreateCacheEntryFilterFactory.Config> {

  private static final Logger LOG = LoggerFactory.getLogger(CreateCacheEntryFilterFactory.class);

  private final CacheConfiguration cacheConfiguration;
  private final Store store;
  private final Rule rule;

  public CreateCacheEntryFilterFactory(final CacheConfiguration cacheConfiguration,
                                       final Store store,
                                       final Rule rule) {
    this.cacheConfiguration = cacheConfiguration;
    this.store = store;
    this.rule = rule;
  }

  @SuppressWarnings("WeakerAccess")
  protected boolean shouldCache(final ServerWebExchange exchange) {
    return rule.applies(exchange);
  }

  @Override
  public GatewayFilter apply(final Config config) {
    return new OrderedGatewayFilter(((exchange, chain) -> {

      if (exchange.getAttributes().get(FindCacheEntryFilterFactory.CACHE_ENTRY_ATTRIBUTE_NAME) != null) {
        LOG.debug("Not caching exchange [{}]. A cache entry is available in the exchange.", exchange);
        return chain.filter(exchange);
      }

      if (!shouldCache(exchange)) {
        LOG.debug("Not caching exchange [{}].", exchange);
        return chain.filter(exchange);
      }

      if (cacheConfiguration.isExposeCacheEventHeader()) {
        exchange.getResponse().getHeaders().add(cacheConfiguration.getCacheEventHeaderName(), "store");
      }

      return chain.filter(store.write(exchange));
    }), WebClientWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1);
  }

  public static class Config {

  }
}
