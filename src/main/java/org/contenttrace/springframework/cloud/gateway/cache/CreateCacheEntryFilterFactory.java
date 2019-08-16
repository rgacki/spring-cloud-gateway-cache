package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.Rule;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.WebClientWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

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

  @Override
  public GatewayFilter apply(final Config config) {
    return new OrderedGatewayFilter(new CreateCacheEntryFilter(cacheConfiguration, store, rule),
      WebClientWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1);
  }

  public static class Config {

  }
}
