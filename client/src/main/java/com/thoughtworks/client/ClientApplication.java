package com.thoughtworks.client;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableWebFlux
public class ClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClientApplication.class, args);
  }

  @Bean
  WebClientCustomizer configureWebclient(@Value("${downstream.ssl.trust-store}") String trustStorePath, @Value("${downstream.ssl.trust-store-password}") String trustStorePass,
                                         @Value("${downstream.ssl.key-store}") String keyStorePath, @Value("${downstream.ssl.key-store-password}") String keyStorePass, @Value("${downstream.ssl.key-alias}") String keyAlias) {

    return (WebClient.Builder webClientBuilder) -> {
      SslContext sslContext;
      final PrivateKey privateKey;
      final X509Certificate[] certificates;
      try {
        final KeyStore trustStore;
        final KeyStore keyStore;
        trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(new FileInputStream(ResourceUtils.getFile(trustStorePath)), trustStorePass.toCharArray());
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(ResourceUtils.getFile(keyStorePath)), keyStorePass.toCharArray());
        certificates = Collections.list(trustStore.aliases())
            .stream()
            .filter(t -> {
              try {
                return trustStore.isCertificateEntry(t);
              } catch (KeyStoreException e1) {
                throw new RuntimeException("Error reading truststore", e1);
              }
            })
            .map(t -> {
              try {
                return trustStore.getCertificate(t);
              } catch (KeyStoreException e2) {
                throw new RuntimeException("Error reading truststore", e2);
              }
            }).toArray(X509Certificate[]::new);
        privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyStorePass.toCharArray());
        Certificate[] certChain = keyStore.getCertificateChain(keyAlias);
        X509Certificate[] x509CertificateChain = Arrays.stream(certChain)
            .map(certificate -> (X509Certificate) certificate)
            .collect(Collectors.toList())
            .toArray(new X509Certificate[certChain.length]);
        sslContext = SslContextBuilder.forClient()
            .keyManager(privateKey, keyStorePass, x509CertificateChain)
            .trustManager(certificates)
            .build();

        HttpClient httpClient = HttpClient.create()
            .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        webClientBuilder.clientConnector(connector);
      } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException e) {
        throw new RuntimeException(e);
      }
    };
  }

  // Without proper certificate

//  @Bean
//  WebClient.Builder builder() throws SSLException {
//    final SslContext sslContext = SslContextBuilder
//        .forClient()
//        .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
//    var tcpClient = TcpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
//    var httpClient = HttpClient.from(tcpClient);
//    return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
//  }
}
