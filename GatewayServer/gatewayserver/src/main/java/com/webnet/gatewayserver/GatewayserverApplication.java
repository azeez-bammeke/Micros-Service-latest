package com.webnet.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
public class GatewayserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayserverApplication.class, args);
    }

    @Bean
    public RouteLocator webnetBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(appRoute -> appRoute.path("/webnet/accounts/**")
                        .filters(f -> f.rewritePath("/webnet/accounts/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .addResponseHeader("X-Webnetnet-client-id", UUID.randomUUID().toString())
                                .circuitBreaker(config -> config.setName("accountCircuitBreaker")
                                        .setFallbackUri("forward:/contactSupport"))
                        )
                        .uri("lb://ACCOUNTS")).route(appRoute -> appRoute.path("/webnet/loans/**")
                        .filters(f -> f.rewritePath("/webnet/loans/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .addResponseHeader("X-Webnetnet-client-id", UUID.randomUUID().toString())
                                .retry(retryConfig -> retryConfig.setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)
                                )
                        )
                        .uri("lb://LOANS")).route(appRoute -> appRoute.path("/webnet/cards/**")
                        .filters(f -> f.rewritePath("/webnet/cards/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .addResponseHeader("X-Webnetnet-client-id", UUID.randomUUID().toString())
                        )
                        .uri("lb://CARDS"))
                .build();
    }

}
