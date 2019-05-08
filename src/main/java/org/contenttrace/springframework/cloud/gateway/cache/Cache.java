package org.contenttrace.springframework.cloud.gateway.cache;

import org.springframework.web.server.ServerWebExchange;

public interface Cache {

  ServerWebExchange write(ServerWebExchange exchange);

}
