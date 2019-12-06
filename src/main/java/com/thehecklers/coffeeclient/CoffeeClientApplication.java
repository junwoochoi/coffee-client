package com.thehecklers.coffeeclient;

import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Instant;

@SpringBootApplication
public class CoffeeClientApplication {

    @Bean
    WebClient webClient() {
        return WebClient.create("http://localhost:18080");
    }

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder.connectTcp("localhost", 18082).block();
    }

    public static void main(String[] args) {
        SpringApplication.run(CoffeeClientApplication.class, args);
    }

}

@RestController
@RequiredArgsConstructor
class RSController {

    private final RSocketRequester requester;

    @GetMapping("/coffees")
    Flux<Coffee> coffees() {
        return requester.route("coffees").retrieveFlux(Coffee.class);
    }

    @GetMapping(value = "/orders/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<CoffeeOrder> orders(@PathVariable String name) {
        return requester.route("orders.".concat(name)).retrieveFlux(CoffeeOrder.class);
    }
}

@Component
@RequiredArgsConstructor
class TestClient {
    private final WebClient webClient;

//    @PostConstruct
    void letsDoThis() {
		webClient.get()
				.uri("/coffees")
				.retrieve()
				.bodyToFlux(Coffee.class)
				.filter(coffee -> coffee.getName().equalsIgnoreCase("kona"))
				.flatMap(
						coffee -> webClient.get()
								.uri("/coffees/{id}/orders", coffee.getId())
								.retrieve()
								.bodyToFlux(CoffeeOrder.class))
				.subscribe(System.out::println);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CoffeeOrder {
    private String coffeeId;
    private Instant whenOrdered;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Coffee {
    private String id;
    private String name;
}
