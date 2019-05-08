package org.contenttrace.springframework.cloud.gateway.cache.fs;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class HostPathPrefixResolutionStrategyTest {

  private MockServerWebExchange createExchange(String hostHeader) {
    final MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest.get("/somepath");
    if (hostHeader != null) {
      request.header("Host", hostHeader);
    }
    return MockServerWebExchange.from(request.build());
  }

  @Test
  void shouldCreateHostBasedPathPrefixWithHostname() {
    // Given
    final ServerWebExchange exchange = createExchange("acme.com");
    final HostPathPrefixResolutionStrategy strategy =
      new HostPathPrefixResolutionStrategy( ex -> Paths.get("somepath"));

    // When
    final Path result = strategy.resolve(exchange);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getNameCount());
    assertEquals(Paths.get("acme.com"), result.getName(0));
    assertEquals(Paths.get("somepath"), result.getName(1));
  }

  @Test
  void shouldCreateHostBasedPathPrefixWithHostnameAndPort() {
    // Given
    final ServerWebExchange exchange = createExchange("acme.com:8080");
    final HostPathPrefixResolutionStrategy strategy =
      new HostPathPrefixResolutionStrategy( ex -> Paths.get("somepath"));

    // When
    final Path result = strategy.resolve(exchange);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getNameCount());
    assertEquals(Paths.get("acme.com_8080"), result.getName(0));
    assertEquals(Paths.get("somepath"), result.getName(1));
  }

  @Test
  void shouldCreateHostBasedPathPrefixWithDefault() {
    // Given
    final ServerWebExchange exchange = createExchange(null);
    final HostPathPrefixResolutionStrategy strategy =
      new HostPathPrefixResolutionStrategy( ex -> Paths.get("somepath"));

    // When
    final Path result = strategy.resolve(exchange);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getNameCount());
    assertEquals(Paths.get(strategy.getDefaultName()), result.getName(0));
    assertEquals(Paths.get("somepath"), result.getName(1));
  }

  @Test
  void shouldCreateHostBasedPathPrefixWithCustomDefaultName() {
    // Given
    final ServerWebExchange exchange = createExchange(null);
    final HostPathPrefixResolutionStrategy strategy =
      new HostPathPrefixResolutionStrategy( ex -> Paths.get("somepath"), "custom-name");

    // When
    final Path result = strategy.resolve(exchange);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getNameCount());
    assertEquals("custom-name", strategy.getDefaultName());
    assertEquals(Paths.get("custom-name"), result.getName(0));
    assertEquals(Paths.get("somepath"), result.getName(1));
  }

}