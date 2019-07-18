package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.CreateCacheEntryFilter;
import org.contenttrace.springframework.cloud.gateway.cache.rules.StandardRules;
import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.StandardCacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.test.BaseWebClientTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=62175"})
@DirtiesContext
class InMemoryStoreTests extends BaseWebClientTests {

  @Test
  public void testResposneParameterFilter() {
    URI uri = UriComponentsBuilder.fromUriString(this.baseUri + "/get").build(true)
      .toUri();
    String host = "www.addresponseparamjava.org";
    String expectedValue = "myresponsevalue";
    testClient.get().uri(uri).header("Host", host).exchange().expectHeader()
      .valueEquals("example", expectedValue);
  }

  @EnableAutoConfiguration
  @SpringBootConfiguration
  @Import(DefaultTestConfig.class)
  public static class TestConfig {

    @Value("${test.uri}")
    String uri;

    @Bean
    public RouteLocator testRouteLocator(final RouteLocatorBuilder builder) {

      final CacheKeyProducer cacheKeyProducer = StandardCacheKeyProducer.getInstance();
      final InMemoryStoreConfiguration storeProperties = new InMemoryStoreConfiguration();
      final InMemoryStore store = new InMemoryStore(cacheKeyProducer, storeProperties);

      final GatewayFilter createCacheEntryFilter =
        new OrderedGatewayFilter(new CreateCacheEntryFilter(store, StandardRules.httpCompliant()), NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1);

      return builder.routes()
        .route("cache_active", r -> r.path("/**").filters(f -> f.filter(createCacheEntryFilter)).uri(uri))
        .build();

    }

  }

}