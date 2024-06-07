package com.ynaitbelabs.ioinspector.configuration;


import com.ynaitbelabs.ioinspector.postprocessor.CustomBeanPostProcessor;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Configuration
public class Config {

    public static final String URL = "https://www.7timer.info/bin/astro.php?lon=113.2&lat=23.1&ac=0&unit=metric&output=json&tzshift=0";

    @Bean
    public WebClient webClient() throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        HttpClient httpClient = HttpClient.create()
                .secure(sslProviderBuilder -> sslProviderBuilder.sslContext(sslContext));
        WebClient.Builder webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(URL)
                .filter(ExchangeFilterFunction.ofRequestProcessor(req -> {
                    log.info("Target URL {}, headers {}\n", req.url(), req.headers());
                    return Mono.just(req);
                }))
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Response status code {}", clientResponse.statusCode().value());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.trace("Body is {}", body);
                                    return Mono.just(clientResponse);
                                });
                    }
                    return Mono.just(clientResponse);
                }))
                .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE);
        return webClientBuilder.build();
    }

    @Bean
    public CustomBeanPostProcessor customBeanPostProcessor(){
        return new CustomBeanPostProcessor();
    }
}
