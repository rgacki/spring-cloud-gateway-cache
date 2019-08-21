package org.contenttrace.springframework.cloud.gateway.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(CacheAutoConfiguration.class)
public class InMemoryCacheTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(InMemoryCacheTestApplication.class, args);
  }

  @Value("${test.uri:http://test-backend.org:80}")
  String backendUri;

  @Bean
  public CacheConfiguration cacheConfiguration() {
    final CacheConfiguration configuration = new CacheConfiguration();
    configuration.setExposeCacheEventHeader(true);
    return configuration;
  }

  @Bean
  public RouteLocator customRouteLocator(final RouteLocatorBuilder builder,
                                         final CreateCacheEntryFilterFactory createCacheEntryFilterFactory,
                                         final FindCacheEntryFilterFactory findCacheEntryFilterFactory) {

    final GatewayFilter createCacheEntry =
      createCacheEntryFilterFactory.apply(new CreateCacheEntryFilterFactory.Config());
    final GatewayFilter findCacheEntry =
      findCacheEntryFilterFactory.apply(new FindCacheEntryFilterFactory.Config());

    return builder.routes()
      .route(r -> r.host("cached.org", "**.cached.org")
        .filters(
          filters -> filters.filters(createCacheEntry, findCacheEntry)
        )
        .uri(backendUri))
      .build();
  }

  @Bean
  public CacheTestController integrationTestController() {
    return new CacheTestController();
  }
}
