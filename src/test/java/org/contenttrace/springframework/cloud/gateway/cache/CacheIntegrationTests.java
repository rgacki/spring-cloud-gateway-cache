package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.store.ResourceCachedEvent;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.contenttrace.springframework.cloud.gateway.cache.test2.IntegrationTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    client().get().uri("/cacheable").header("Host", CACHED_HOST)
      .exchange()
      .expectStatus().isOk()
      .expectHeader().valueEquals("X-Cache-Event", "store")
      .expectBody()
      .consumeWith(result -> {
        final byte[] responseBody = result.getResponseBody();
        Assertions.assertNotNull(responseBody);
      });

    // THen
    testEvents.assertLast().isOfType(ResourceCachedEvent.class);
  }

}
