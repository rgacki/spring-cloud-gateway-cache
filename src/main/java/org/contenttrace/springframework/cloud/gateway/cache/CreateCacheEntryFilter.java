package org.contenttrace.springframework.cloud.gateway.cache;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class CreateCacheEntryFilter implements GatewayFilter {

  private final Cache cache;

  public CreateCacheEntryFilter(final Cache cache) {
    Objects.requireNonNull(cache, "'cache' must not be null!");
    this.cache = cache;
  }

  @Override
  public Mono<Void> filter(final ServerWebExchange exchange,
                           final GatewayFilterChain chain) {
    return chain.filter(cache.write(exchange));
  }
}
