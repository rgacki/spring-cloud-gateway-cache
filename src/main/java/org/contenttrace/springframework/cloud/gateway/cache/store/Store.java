package org.contenttrace.springframework.cloud.gateway.cache.store;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

public interface Store {

  /**
   * <p>Mutates the exchange to have its response written to the store and returns it. Only works if a response is
   * available in the exchange, which has not be read from, yet.</p>
   *
   * @param exchange the exchange
   * @return the exchange
   */
  ServerWebExchange write(ServerWebExchange exchange);

  /**
   * Finds a stored cache entry for the given request. Returns nothing if the request does not match any stored entries.
   *
   * @param request the request
   * @return the entry
   */
  Optional<? extends Entry> find(ServerHttpRequest request);

}
