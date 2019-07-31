package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.StandardRules;
import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.StandardCacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.contenttrace.springframework.cloud.gateway.cache.store.inmemory.InMemoryStoreConfiguration;
import org.contenttrace.springframework.cloud.gateway.cache.store.inmemory.InMemoryStoreFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@EnableAutoConfiguration
public class InMemoryCacheTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(InMemoryCacheTestApplication.class, args);
  }

  @Value("${test.uri:http://test-backend.org:80}")
  String backendUri;

  @Bean
  public Store defaultCacheStore() {

    final InMemoryStoreFactory storeFactory = new InMemoryStoreFactory();
    final CacheKeyProducer cacheKeyProducer = StandardCacheKeyProducer.getInstance().includeHost();
    final InMemoryStoreConfiguration configuration = new InMemoryStoreConfiguration();

    return storeFactory.createInstance(cacheKeyProducer, configuration);
  }

  @Bean
  public RouteLocator customRouteLocator(final RouteLocatorBuilder builder, final Store store) {
    return builder.routes()
      .route(r -> r.host("cached.org", "**.cached.org")
        .filters(
          filters -> filters.filter(new CreateCacheEntryFilter(store, StandardRules.httpCompliant()))
        )
        .uri(backendUri))
      .build();
  }

  @Bean
  public CacheTestController integrationTestController() {
    return new CacheTestController();
  }
}
