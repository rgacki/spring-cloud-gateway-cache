package org.contenttrace.springframework.cloud.gateway.cache.fs;

import org.springframework.web.server.ServerWebExchange;

import java.nio.file.Path;

@FunctionalInterface
public interface PathResolutionStrategy {

  Path resolve(ServerWebExchange exchange);

}
