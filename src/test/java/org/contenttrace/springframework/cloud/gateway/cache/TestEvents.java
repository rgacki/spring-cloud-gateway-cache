package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.store.Event;
import org.contenttrace.springframework.cloud.gateway.cache.store.EventListener;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;

import java.util.LinkedList;

public class TestEvents {

  private final LinkedList<Event> events;

  private final EventListener listener = new EventListener() {
    @Override
    public void dispatched(Event event) {
      events.add(event);
    }
  };

  public TestEvents() {
    this.events = new LinkedList<>();
  }

  public void registerWith(Store store) {
    store.events().register(listener);
  }

  public void unregisterFrom(Store store) {
    store.events().unregister(listener);
  }

}
