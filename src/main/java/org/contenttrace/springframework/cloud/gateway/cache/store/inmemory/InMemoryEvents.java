/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.*;
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

	void publishResourceCached(final ServerWebExchange exchange, final Entry entry) {
		publish(new InMemoryResourceCachedEvent(exchange.getRequest().getURI(), entry));
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
		private final Entry entry;

		InMemoryResourceCachedEvent(final URI uri,
									final Entry entry) {
			this.uri = uri;
			this.entry = entry;
		}

		@Override
		public URI getURI() {
			return uri;
		}

		@Override
		public Entry getEntry() {
			return entry;
		}


	}

}
