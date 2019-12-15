package com.thoughtworks.server;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class Accounts {

  @GetMapping("/data")
  public Flux<String> accounts() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .flatMapMany(authentication -> Flux.just(authentication.getName(), " Awesome", " Great", " it", " works!"));
  }
}