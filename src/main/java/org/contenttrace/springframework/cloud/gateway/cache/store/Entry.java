package org.contenttrace.springframework.cloud.gateway.cache.store;

import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * A stored cache entry.
 */
public interface Entry {

  /**
   * Returns the HTTP method the entry was created for.
   *
   * @return the HTTP method
   */
  String getHttpMethod();

  /**
   * Returns the host the entry was created for.
   *
   * @return the host
   */
  Optional<String> getHost();

  /**
   * Returns the path the entry was created for.
   *
   * @return the path
   */
  String getPath();

  /**
   * Returns the query the entry was created for.
   *
   * @return the query
   */
  Optional<String> getQuery();

  /**
   * Returns the request headers, the entry was created for. The headers may be empty, if the original response does not
   * provide a "Vary" header. The headers returned are only those mentioned by the "Vary" header.
   *
   * @return the request headers
   */
  HttpHeaders getRequestHeaders();

  /**
   * Returns the headers of the original response. The headers are not filtered.
   *
   * @return the response headers
   */
  HttpHeaders getResponseHeaders();

  /**
   * Returns the data of the entry.
   *
   * @return the data
   * @throws IOException if the stream cannot be produced
   */
  InputStream openStream() throws IOException;

  /**
   * Returns the size of the data. May return {@code -1L} if there is no data.
   *
   * @return the size or {@code -1L} if there is no data
   */
  long size();

  /**
   * Invalidates this cache entry. This method must be side-effect free. So for example, if the entry is already
   * invalidated or if an I/O related issue occurs, the method should return without an error. Any errors must be
   * handled by the implementation internally.
   */
  void invalidate();

}
