package com.thoughtworks.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    SubjectDnX509PrincipalExtractor principalExtractor =
        new SubjectDnX509PrincipalExtractor();

    principalExtractor.setSubjectDnRegex("OU=(.*?)(?:,|$)");

    ReactiveAuthenticationManager authenticationManager = authentication -> {
      authentication.setAuthenticated("nt".equals(authentication.getName()));
      return Mono.just(authentication);
    };

    http
        .x509(x509 ->
            x509
                .principalExtractor(principalExtractor)
                .authenticationManager(authenticationManager)).authorizeExchange(exchanges ->
            exchanges
                .anyExchange().authenticated());
    return http.build();
  }
}
