package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.store.ResourceCachedEvent;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.contenttrace.springframework.cloud.gateway.cache.test2.IntegrationTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

public abstract class CacheIntegrationTests extends IntegrationTests {

  static final String CACHED_HOST = "www.cached.org";

  TestEvents testEvents;

  abstract WebTestClient client();

  abstract Store store();

  @BeforeEach
  void setup() {
    testEvents = new TestEvents();
    testEvents.registerWith(store());
  }

  @AfterEach
  void tearDown() {
    testEvents.unregisterFrom(store());
    testEvents = null;
  }

  @Test
  void shouldCacheRequest() {

    // When
    final EntityExchangeResult<byte[]> firstResult = client().get().uri("/cacheable").header("Host", CACHED_HOST)
      .header("X-Test", "shouldCacheRequest(#1)")
      .exchange()
      .expectStatus().isOk()
      .expectHeader().valueEquals("X-Cache", "store")
      .expectBody()
      .returnResult();

    // Then
    testEvents.assertLast().isOfType(ResourceCachedEvent.class);
    Assertions.assertNotNull(firstResult);

    // When
    final EntityExchangeResult<byte[]> secondResult = client().get().uri("/cacheable").header("Host", CACHED_HOST)
      .header("X-Test", "shouldCacheRequest(#2)")
      .exchange()
      .expectStatus().isOk()
      .expectHeader().valueEquals("X-Cache", "hit", "store")
      .expectBody()
      .returnResult();

    // Then
    Assertions.assertNotNull(secondResult);
    Assertions.assertArrayEquals(firstResult.getResponseBody(), secondResult.getResponseBody());
  }

}
