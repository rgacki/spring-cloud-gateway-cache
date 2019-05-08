package org.contenttrace.springframework.cloud.gateway.cache.fs;

import org.contenttrace.springframework.cloud.gateway.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class FileSystemCache implements Cache {

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemCache.class);

  private final FileSystemCacheProperties properties;

  private final Path rootPath;
  private final PathResolutionStrategy pathResolutionStrategy;

  public FileSystemCache(final FileSystemCacheProperties properties,
                         final PathResolutionStrategy pathResolutionStrategy) {
    requireNonNull(properties, "'properties' must not be null!");
    requireNonNull(pathResolutionStrategy, "'pathResolutionStrategy' must not be null!");
    this.properties = properties;
    this.rootPath = resolveRootPath(properties);
    this.pathResolutionStrategy = pathResolutionStrategy;
  }

  protected Path resolveRootPath(final FileSystemCacheProperties properties) {
    final Path configuredPath = properties.getRoot();
    if (configuredPath != null) {
      try {
        Files.createDirectories(configuredPath);
      } catch (IOException e) {
        throw new InternalFileSystemCacheException(String.format("Failed to create root path at [%s]!", configuredPath));
      }
      return configuredPath;
    }
    try {
      return Files.createTempDirectory("spring-cloud-gateway-cache-");
    } catch (IOException e) {
      throw new InternalFileSystemCacheException("Failed to create temporary root directory!", e);
    }
  }

  protected boolean shouldCache(final ServerWebExchange exchange) {
    return HttpMethod.GET == exchange.getRequest().getMethod();
  }

  @Override
  public ServerWebExchange write(final ServerWebExchange exchange) {
    requireNonNull(exchange, "'exchange' must not be null!");

    if (!shouldCache(exchange)) {
      LOG.debug("Not caching exchange [{}].", exchange);
      return exchange;
    }



    return null;
  }
}
