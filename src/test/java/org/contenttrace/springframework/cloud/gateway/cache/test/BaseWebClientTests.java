package org.contenttrace.springframework.cloud.gateway.cache.test;

import java.time.Duration;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_HANDLER_MAPPER_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * @author Spencer Gibb
 */
@ExtendWith(SpringExtension.class)
public abstract class BaseWebClientTests {

  protected static final String HANDLER_MAPPER_HEADER = "X-Gateway-Handler-Mapper-Class";

  protected static final String ROUTE_ID_HEADER = "X-Gateway-RouteDefinition-Id";

  protected static final Duration DURATION = Duration.ofSeconds(5);

  @LocalServerPort
  protected int port = 0;

  protected WebTestClient testClient;

  protected WebClient webClient;

  protected String baseUri;

  @BeforeEach
  public void setup() {
    setup(new ReactorClientHttpConnector(), "http://localhost:" + port);
  }

  protected void setup(ClientHttpConnector httpConnector, String baseUri) {
    this.baseUri = baseUri;
    this.webClient = WebClient.builder().clientConnector(httpConnector)
      .baseUrl(this.baseUri).build();
    this.testClient = WebTestClient.bindToServer(httpConnector).baseUrl(this.baseUri)
      .build();
  }

  @Configuration
  @RibbonClients({
    @RibbonClient(name = "testservice", configuration = TestRibbonConfig.class)
  })
  @Import(PermitAllSecurityConfiguration.class)
  public static class DefaultTestConfig {

    private static final Log log = LogFactory.getLog(DefaultTestConfig.class);

    @Bean
    public HttpBinCompatibleController httpBinController() {
      return new HttpBinCompatibleController();
    }

    @Bean
    @Order(500)
    public GlobalFilter modifyResponseFilter() {
      return (exchange, chain) -> {
        log.info("modifyResponseFilter start");
        String value = exchange.getAttributeOrDefault(GATEWAY_HANDLER_MAPPER_ATTR,
          "N/A");
        exchange.getResponse().getHeaders().add(HANDLER_MAPPER_HEADER, value);
        Route route = exchange.getAttributeOrDefault(GATEWAY_ROUTE_ATTR, null);
        if (route != null) {
          exchange.getResponse().getHeaders().add(ROUTE_ID_HEADER,
            route.getId());
        }
        return chain.filter(exchange);
      };
    }

  }

  @EnableAutoConfiguration
  @SpringBootConfiguration
  @Import(DefaultTestConfig.class)
  public static class MainConfig {

  }

  protected static class TestRibbonConfig {

    @LocalServerPort
    protected int port = 0;

    @Bean
    public ServerList<Server> ribbonServerList() {
      return new StaticServerList<>(new Server("localhost", this.port));
    }

  }

}