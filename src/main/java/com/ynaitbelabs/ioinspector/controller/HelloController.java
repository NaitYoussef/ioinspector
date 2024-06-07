package com.ynaitbelabs.ioinspector.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class HelloController {

    private final WebClient webClient;

    public HelloController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/hello")
    public String hello() {
        return webClient.get().retrieve().bodyToMono(String.class).block();
    }
}
