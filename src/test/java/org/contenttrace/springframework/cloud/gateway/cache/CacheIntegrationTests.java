package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.store.Entry;
import org.contenttrace.springframework.cloud.gateway.cache.store.ResourceCachedEvent;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    testEvents.assertLast().isOfType(ResourceCachedEvent.class).satisfies(event -> {
      assertNotNull(event.getEntry());
      final Entry entry = event.getEntry();
      assertThat(entry.getKey()).isNotNull();
      assertThat(entry.getKey().serializeAsString()).isNotNull().isNotEmpty();
      assertEquals("GET", entry.getHttpMethod());
      assertEquals(CACHED_HOST, entry.getHost().orElse(null));
      assertEquals("/cacheable",entry.getPath());
    });
    assertNotNull(firstResult);

    // When
    final EntityExchangeResult<byte[]> secondResult = client().get().uri("/cacheable").header("Host", CACHED_HOST)
      .header("X-Test", "shouldCacheRequest(#2)")
      .exchange()
      .expectStatus().isOk()
      .expectHeader().valueEquals("X-Cache", "hit")
      .expectBody()
      .returnResult();

    // Then
    assertNotNull(secondResult);
    Assertions.assertArrayEquals(firstResult.getResponseBody(), secondResult.getResponseBody());
  }

}
