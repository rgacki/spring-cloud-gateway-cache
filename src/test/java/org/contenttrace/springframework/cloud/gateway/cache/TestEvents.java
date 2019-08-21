package org.contenttrace.springframework.cloud.gateway.cache;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.contenttrace.springframework.cloud.gateway.cache.store.Event;
import org.contenttrace.springframework.cloud.gateway.cache.store.EventListener;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;

import java.util.Iterator;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEvents {

  private final LinkedList<Event> events;

  private final EventListener listener = new EventListener() {
    @Override
    public void dispatched(Event event) {
      synchronized (events) {
        events.add(event);
      }
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

  public EventAssert<?> assertLast() {
    return assertNth(1);
  }

  public EventAssert<?> assertNth(int nthLastEvent) {
    int count = 0;
    Event event = null;
    synchronized (events) {
      final Iterator<Event> eventIterator = events.descendingIterator();
      while (eventIterator.hasNext() && count < nthLastEvent) {
        event = eventIterator.next();
        count = count + 1;
      }
    }
    if (count != nthLastEvent || event == null) {
      Assertions.fail(String.format("There is no %s-nth event!", nthLastEvent));
    }
    return new EventAssert<>(event);
  }

  public static abstract class AbstractEventAssert<SELF extends AbstractEventAssert<SELF, EVENT>, EVENT extends Event>
    extends AbstractAssert<SELF, EVENT> {

    public AbstractEventAssert(EVENT event, Class<?> selfType) {
      super(event, selfType);
    }
  }

  public static class EventAssert<E extends Event> extends AbstractEventAssert<EventAssert<E>, E> {

    private final E event;

    public EventAssert(E event) {
      super(event, EventAssert.class);
      this.event = event;
    }

    public E getEvent() {
      return event;
    }

    public <T extends Event> EventAssert<T> isOfType(Class<T> eventType) {
      assertThat(event).isInstanceOf(eventType);
      return new EventAssert<>(eventType.cast(event));
    }

  }

}
