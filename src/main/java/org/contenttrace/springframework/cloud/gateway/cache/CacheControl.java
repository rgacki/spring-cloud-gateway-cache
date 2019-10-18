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
package org.contenttrace.springframework.cloud.gateway.cache;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CacheControl {

	public static class Directive {

		public static Directive of(final String name) {
			requireNonNull(name, "'name' must not be null!");
			return new Directive(name);
		}

		private final String name;

		private Directive(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Directive directive = (Directive) o;
			return name.equals(directive.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
	}

	public static final Directive NO_CACHE = Directive.of("no-cache");
	public static final Directive NO_STORE = Directive.of("no-store");
	public static final Directive NO_TRANSFORM = Directive.of("no-transform");
	public static final Directive ONLY_IF_CACHHED = Directive.of("only-if-cached");
	public static final Directive MUST_REVALIDATE = Directive.of("must-revalidate");
	public static final Directive PUBLIC = Directive.of("public");
	public static final Directive PRIVATE = Directive.of("private");
	public static final Directive PROXY_REVALIDATE = Directive.of("proxy-revalidate");
	public static final Directive IMMUTABLE = Directive.of("immutable");

	private static final CacheControl EMPTY = new CacheControl(Collections.emptyList());

	public static CacheControl empty() {
		return EMPTY;
	}

	public static CacheControl parse(final String value) {
		return parse(value, true);
	}

	public static CacheControl parse(final String value, final boolean omitInvalidDirectives) {
		if (value == null) {
			return empty();
		}
		final StringTokenizer tokenizer = new StringTokenizer(value, ",");
		final List<Directive> directives = new ArrayList<>(3);
		while (tokenizer.hasMoreTokens()) {
			final String token = tokenizer.nextToken().trim();
			final int delimIndex = token.indexOf('=');
			if (delimIndex < 0) {
				directives.add(new Directive(token));
			} else if (delimIndex > 0 && token.length() > (delimIndex + 1)) {
				final String directiveName = token.substring(0, delimIndex);
				final String directiveValue = token.substring(delimIndex + 1);
				try {
					directives.add(new TimeDirective(directiveName, Duration.ofSeconds(Long.parseLong(directiveValue))));
				} catch (final NumberFormatException e) {
					if (!omitInvalidDirectives) {
						throw new IllegalArgumentException(String.format("Unexpected value [%s] for directive [%s] in [%s]!", directiveValue, directiveName, value));
					}
				}
			}
		}
		return new CacheControl(directives);
	}


	public static class TimeDirective extends Directive {

		public static TimeDirective of(final String name, final Duration duration) {
			requireNonNull(name, "'name' must not be null!");
			requireNonNull(name, "'duration' must not be null!");
			return new TimeDirective(name, duration);
		}

		private final Duration duration;

		private TimeDirective(final String name, final Duration duration) {
			super(name);
			this.duration = duration;
		}

		public Duration getDuration() {
			return duration;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			TimeDirective that = (TimeDirective) o;
			return duration.equals(that.duration);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), duration);
		}
	}

	private final Map<String, Directive> directives;

	public CacheControl(final List<Directive> directives) {
		this.directives = directives.stream()
				.collect(Collectors.toMap(
						directive -> directive.getName().toLowerCase(Locale.ENGLISH),
						directive -> directive
				));
	}

	public Directive get(final String name) {
		return directives.get(name.toLowerCase(Locale.ENGLISH));
	}

	public boolean has(final String name) {
		return directives.containsKey(name.toLowerCase(Locale.ENGLISH));
	}

	public boolean has(final Directive directive) {
		return directives.containsValue(directive);
	}

}
