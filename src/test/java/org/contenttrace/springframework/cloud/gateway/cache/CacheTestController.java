package org.contenttrace.springframework.cloud.gateway.cache;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
public class CacheTestController {

  private final ZonedDateTime lastModified = ZonedDateTime.now();

  @GetMapping(path = "/tests/cacheable")
  public ResponseEntity<String> cacheable() {
    return ResponseEntity.status(HttpStatus.OK)
      .contentType(MediaType.TEXT_PLAIN)
      .lastModified(lastModified)
      .body("1");
  }

  @GetMapping(path = "/tests/non-cacheable")
  public ResponseEntity<String> nonCacheable() {
    return ResponseEntity.status(HttpStatus.OK)
      .contentType(MediaType.TEXT_PLAIN)
      .cacheControl(CacheControl.noCache())
      .body("1");
  }

}
