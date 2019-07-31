package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.contenttrace.springframework.cloud.gateway.cache.test2.IntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.ExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

public abstract class CacheIntegrationTests extends IntegrationTests {

  static final String CACHED_HOST = "www.cached.org";

  abstract WebTestClient client();

  abstract Store store();



  @Test
  void shouldCacheRequest() {

    // When
    client().get().uri("/cacheable").header("Host", CACHED_HOST)
      .exchange()
      .expectStatus().isOk()
      .expectHeader().valueEquals("X-Cache-Event", "store")
      .expectBody()
      .consumeWith(ExchangeResult::getResponseBodyContent);
  }

}
