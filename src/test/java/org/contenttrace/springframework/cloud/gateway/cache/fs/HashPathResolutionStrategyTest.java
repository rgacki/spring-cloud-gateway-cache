package org.contenttrace.springframework.cloud.gateway.cache.fs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HashPathResolutionStrategyTest {

  private ServerWebExchange createExchange() {
    return MockServerWebExchange.from(MockServerHttpRequest.get("/foo").build());
  }

  @Test
  void shouldCreatePathStructure() {
    // Given
    final ServerWebExchange exchange = createExchange();
    final HashPathResolutionStrategy strategy = new HashPathResolutionStrategy();

    // When
    final Path result = strategy.resolve(exchange);

    // Then

    assertNotNull(result, "result is not null");
    assertFalse(result.isAbsolute(), "path is not absolute");
    assertTrue(result.getNameCount() > 1);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 10, Integer.MAX_VALUE})
  void shouldCreatePathStructureWithCustomPrefixSegments(final int maximumPathPrefixSegments) {
    // Given
    final ServerWebExchange exchange = createExchange();
    final HashPathResolutionStrategy strategy = new HashPathResolutionStrategy(maximumPathPrefixSegments);

    // When
    final Path result = strategy.resolve(exchange);

    // Then

    assertNotNull(result, "result is not null");
    assertFalse(result.isAbsolute(), "path is not absolute");
    assertTrue(result.getNameCount() > 1);
  }

  @ParameterizedTest
  @ValueSource(ints = {Integer.MIN_VALUE, -1, 0})
  void shouldCreatePathWithoutPrefix(final int maximumPathPrefixSegments) {
    // Given
    final ServerWebExchange exchange = createExchange();
    final HashPathResolutionStrategy strategy = new HashPathResolutionStrategy(maximumPathPrefixSegments);

    // When
    final Path result = strategy.resolve(exchange);

    // Then

    assertNotNull(result, "result is not null");
    assertFalse(result.isAbsolute(), "path is not absolute");
    assertEquals(1, result.getNameCount());
  }

}