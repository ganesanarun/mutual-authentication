package com.thoughtworks.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/messages")
public class Messages {
  @GetMapping("/data")
  public Flux<String> messages() {
    return Flux.just("It works", " without certificate authentication");
  }
}
