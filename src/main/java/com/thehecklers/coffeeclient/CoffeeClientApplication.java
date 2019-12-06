package com.thehecklers.coffeeclient;

import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.time.Instant;

@SpringBootApplication
public class CoffeeClientApplication {

    @Bean
    WebClient webClient() {
        return WebClient.create("http://localhost:18080");
    }

    public static void main(String[] args) {
        SpringApplication.run(CoffeeClientApplication.class, args);
    }

}

@Component
@RequiredArgsConstructor
class TestClient {
    private final WebClient webClient;

    @PostConstruct
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
