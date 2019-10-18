package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.Rule;
import org.contenttrace.springframework.cloud.gateway.cache.rules.StandardRules;
import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.StandardCacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.contenttrace.springframework.cloud.gateway.cache.store.StoreFactory;
import org.contenttrace.springframework.cloud.gateway.cache.store.inmemory.InMemoryStoreConfiguration;
import org.contenttrace.springframework.cloud.gateway.cache.store.inmemory.InMemoryStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import java.util.List;

@Configuration
@AutoConfigureBefore({GatewayAutoConfiguration.class})
@EnableConfigurationProperties(CacheConfiguration.class)
public class CacheAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public CacheKeyProducer cacheKeyProducer() {
    return StandardCacheKeyProducer.getInstance().includeHost();
  }

  @Bean
  @ConditionalOnMissingBean
  public InMemoryStoreFactory cacheStoreFactory() {
    // TODO: The configuration must be externalized.
    final InMemoryStoreConfiguration configuration = new InMemoryStoreConfiguration();
    return new InMemoryStoreFactory(configuration);
  }

  @Bean
  @Primary
  public Store cacheStore(final CacheKeyProducer cacheKeyProducer,
                          final StoreFactory storeFactory) {
    return storeFactory.createInstance(cacheKeyProducer);
  }

  @Bean
  @ConditionalOnMissingBean
  public Rule cacheRule() {
    return StandardRules.httpCompliant();
  }

  @Bean
  @ConditionalOnMissingBean
  public CreateCacheEntryFilterFactory createCacheEntryFilterFactory(final CacheConfiguration cacheConfiguration,
                                                                     final Store cacheStore,
                                                                     final Rule cacheRule) {
    return new CreateCacheEntryFilterFactory(cacheConfiguration, cacheStore, cacheRule);
  }

  @Bean
  @ConditionalOnMissingBean
  public FindCacheEntryFilterFactory findCacheEntryFilterFactory(final CacheConfiguration cacheConfiguration,
                                                                 final Store cacheStore,
                                                                 final Rule cacheRule,
                                                                 final ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider) {
    return new FindCacheEntryFilterFactory(cacheConfiguration, cacheStore, cacheRule, headersFiltersProvider);
  }

  @Bean
  @ConditionalOnMissingBean
  public WriteCachedResponseFilter writeCachedResponseFilter(final GatewayProperties properties) {
    return new WriteCachedResponseFilter(new DefaultDataBufferFactory(), properties.getStreamingMediaTypes());
  }

}
