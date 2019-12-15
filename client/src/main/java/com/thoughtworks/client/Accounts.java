package com.thoughtworks.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RestController
public class Accounts {

  private WebClient webClient;

  public Accounts(WebClient.Builder builder) {
    this.webClient = builder.build();
  }

  @GetMapping("/data")
  public Flux<String> accounts() {
    return webClient.get().uri("https://localhost:9002/data")
        .exchange()
        .flatMapMany(clientResponse -> clientResponse.bodyToFlux(String.class));

//    return Flux.empty();
  }
}
