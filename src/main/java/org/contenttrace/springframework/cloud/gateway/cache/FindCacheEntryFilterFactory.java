package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.Rule;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

public class FindCacheEntryFilterFactory extends AbstractGatewayFilterFactory<FindCacheEntryFilterFactory.Config> {

  private final CacheConfiguration cacheConfiguration;
  private final Store store;
  private final Rule rule;

  public FindCacheEntryFilterFactory(CacheConfiguration cacheConfiguration, Store store, Rule rule) {
    this.cacheConfiguration = cacheConfiguration;
    this.store = store;
    this.rule = rule;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new OrderedGatewayFilter(new FindCacheEntryFilter(cacheConfiguration, store, rule),
      OrderedGatewayFilter.HIGHEST_PRECEDENCE + 1);
  }

  public static class Config {

  }
}
