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

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.SocketUtils;

import java.time.Duration;

@SpringBootTest(
		classes = {InMemoryCacheTests.TestConfig.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "management.server.port=${test.port}"
)
public class InMemoryCacheTests extends CacheIntegrationTests {

	protected static int managementPort;

	@LocalServerPort
	protected int port = 0;

	protected WebTestClient webClient;

	protected String baseUri;

	@Autowired
	Store store;

	@BeforeAll
	static void beforeClass() {
		managementPort = SocketUtils.findAvailableTcpPort();
		System.setProperty("test.port", String.valueOf(managementPort));
	}

	@AfterAll
	static void afterClass() {
		System.clearProperty("test.port");
	}

	@BeforeEach
	void setup() {
		super.setup();
		baseUri = "http://localhost:" + port;
		this.webClient = WebTestClient.bindToServer()
				.responseTimeout(Duration.ofSeconds(10)).baseUrl(baseUri).build();
	}

	@Override
	WebTestClient client() {
		return webClient;
	}

	@Override
	Store store() {
		return store;
	}

	@Configuration
	@EnableAutoConfiguration
	@RibbonClient(name = "tests", configuration = RibbonConfig.class)
	@Import(InMemoryCacheTestApplication.class)
	protected static class TestConfig {

	}

	protected static class RibbonConfig {

		@LocalServerPort
		int port;

		@Bean
		@Primary
		public ServerList<Server> ribbonServerList() {
			return new StaticServerList<>(new Server("localhost", port));
		}

	}
}
