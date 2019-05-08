package org.contenttrace.springframework.cloud.gateway.cache.fs;

import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.util.Objects.requireNonNull;

public class HashPathResolutionStrategy implements PathResolutionStrategy {

  private static final byte[] SLASH_BYTES = "/".getBytes(StandardCharsets.UTF_8);
  private static final byte[] QUESTIONMARK_BYTES = "?".getBytes(StandardCharsets.UTF_8);

  private static final int DEFAULT_MAXIMUM_PREFIX_PATH_SEGMENTS =
    Integer.getInteger(HashPathResolutionStrategy.class.getName() + ".maximumPathSegments", 8);

  private static String encodeHexString(final byte[] byteArray) {
    final StringBuilder sb = new StringBuilder();
    for (byte b : byteArray) {
      sb.append(Character.forDigit((b >> 4) & 0xF, 16));
      sb.append(Character.forDigit((b & 0xF), 16));
    }
    return sb.toString();
  }

  private final int maximumPrefixPathSegments;

  public HashPathResolutionStrategy() {
    this(DEFAULT_MAXIMUM_PREFIX_PATH_SEGMENTS);
  }

  public HashPathResolutionStrategy(int maximumPrefixPathSegments) {
    this.maximumPrefixPathSegments = maximumPrefixPathSegments > 0 ? maximumPrefixPathSegments : 0;
  }

  private MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new InternalFileSystemCacheException("Failed to create digest instance!", e);
    }
  }

  @SuppressWarnings("WeakerAccess")
  protected String createHash(final ServerWebExchange exchange) {
    requireNonNull(exchange, "'exchange' must not be null!");

    final URI uri = exchange.getRequest().getURI();

    final String path = uri.getPath();
    final String query = uri.getQuery();

    final MessageDigest digest = newDigest();

    if (path != null) {
      digest.update(path.getBytes(StandardCharsets.UTF_8));
    } else {
      digest.update(SLASH_BYTES);
    }

    if (query != null) {
      digest.update(QUESTIONMARK_BYTES);
      digest.update(query.getBytes(StandardCharsets.UTF_8));
    }

    return encodeHexString(digest.digest());
  }

  @SuppressWarnings("WeakerAccess")
  protected Path toPath(final String hash) {
    requireNonNull(hash, "'hash' must not be null!");

    if (getMaximumPrefixPathSegments() < 1) {
      return Paths.get(hash);
    }

    if (hash.length() < 2 || hash.length() % 2 != 0) {
      throw new IllegalArgumentException(String.format("Hash must have at least two characters and an even amount of " +
        "characters, but has [%s]!", hash.length()));
    }

    Path path = null;

    for (int i = 1; i <= getMaximumPrefixPathSegments(); i++) {
      final int begin = (i * 2) - 2;
      final int end = i * 2;

      if (hash.length() < end) {
        if (path == null) {
          return Paths.get(hash);
        }
        return path.resolve(hash);
      }

      if (path != null) {
        path = path.resolve(Paths.get(hash.substring(begin, end)));
      } else {
        path = Paths.get(hash.substring(begin, end));
      }
    }

    if (path != null) {
      return path.resolve(hash);
    }

    return Paths.get(hash);
  }

  @Override
  public Path resolve(final ServerWebExchange exchange) {
    requireNonNull(exchange, "'exchange' must not be null!");
    return toPath(createHash(exchange));
  }

  public int getMaximumPrefixPathSegments() {
    return maximumPrefixPathSegments;
  }
}
