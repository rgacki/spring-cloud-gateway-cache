package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.Event;
import org.contenttrace.springframework.cloud.gateway.cache.store.EventListener;
import org.contenttrace.springframework.cloud.gateway.cache.store.Events;
import org.contenttrace.springframework.cloud.gateway.cache.store.ResourceCachedEvent;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

class InMemoryEvents implements Events {

  private final List<EventListener> listeners;

  private final Object registrationMutex = new Object();

  InMemoryEvents() {
    this.listeners = new CopyOnWriteArrayList<>();
  }

  void publish(final InMemoryEvent event) {
    Objects.requireNonNull(event, "Event is required!");
    // TODO: Maybe this should be non-blocking?
    listeners.forEach(listener -> listener.dispatched(event));
  }

  void publishResourceCached(final ServerWebExchange exchange) {
    publish(new InMemoryResourceCachedEvent(exchange.getRequest().getURI()));
  }

  @Override
  public void register(final EventListener listener) {
    synchronized (registrationMutex) {
      if (!listeners.contains(listener)) {
        listeners.add(listener);
      }
    }
  }

  @Override
  public void unregister(final EventListener listener) {
    synchronized (registrationMutex) {
      listeners.remove(listener);
    }
  }

  static abstract class InMemoryEvent implements Event {

    private final LocalDateTime time;

    protected InMemoryEvent() {
      this.time = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getTime() {
      return time;
    }
  }

  static class InMemoryResourceCachedEvent extends InMemoryEvent implements ResourceCachedEvent {

    private final URI uri;

    InMemoryResourceCachedEvent(URI uri) {
      this.uri = uri;
    }

    @Override
    public URI getURI() {
      return uri;
    }


  }

}
