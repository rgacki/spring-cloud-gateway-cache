package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.StandardCacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryStoreFactoryTest {

  @Test
  void shouldCreateStore() {

    // Given
    final CacheKeyProducer cacheKeyProducer = StandardCacheKeyProducer.getInstance();
    final InMemoryStoreConfiguration configuration = new InMemoryStoreConfiguration();
    final InMemoryStoreFactory factory = new InMemoryStoreFactory(configuration);

    // When
    final Store store = factory.createInstance(cacheKeyProducer);

    // Then
    assertNotNull(store);
  }

}