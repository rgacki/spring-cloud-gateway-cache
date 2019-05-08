package org.contenttrace.springframework.cloud.gateway.cache.fs;

import org.springframework.web.server.ServerWebExchange;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.*;

/**
 * A strategy that uses the requests Host header and adds it as a prefix to the path of the wrapped strategy.
 */
public class HostPathPrefixResolutionStrategy implements PathResolutionStrategy {

  private static final String DEFAULT_DEFAULT_NAME =
    System.getProperty(HostPathPrefixResolutionStrategy.class.getName() + ".defaultName", "_default");

  private final PathResolutionStrategy wrapped;
  private final String defaultName;

  public HostPathPrefixResolutionStrategy(final PathResolutionStrategy wrapped) {
    this(wrapped, DEFAULT_DEFAULT_NAME);
  }

  public HostPathPrefixResolutionStrategy(final PathResolutionStrategy wrapped,
                                          final String defaultName) {
    requireNonNull(wrapped, "'wrapped' must not be null!");
    requireNonNull(defaultName, "'defaultName' must not be null!");
    this.wrapped = wrapped;
    this.defaultName = defaultName;
  }

  @Override
  public Path resolve(final ServerWebExchange exchange) {

    final Path childPath = wrapped.resolve(exchange);

    if (childPath.isAbsolute()) {
      throw new IllegalStateException(String.format("The child path [%s] must be relative!", childPath));
    }

    final String host = exchange.getRequest().getHeaders().getFirst("Host");

    if (host == null) {
      return Paths.get(getDefaultName()).resolve(childPath);
    }

    // TODO: Either sanitize the host name or fail for invalid characters. Right now, we just fail for path traversals.

    String sanitizedHost = host.replace(':', '_');

    final Path hostPath = Paths.get(sanitizedHost);

    if(hostPath.getNameCount() != 1) {
      throw new IllegalArgumentException(String.format("Host name [%s] leads to a invalid path!", host));
    }

    return hostPath.resolve(childPath);
  }

  public String getDefaultName() {
    return defaultName;
  }
}
