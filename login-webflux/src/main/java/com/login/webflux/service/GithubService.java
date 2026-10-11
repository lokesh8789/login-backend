package com.login.webflux.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GithubService {
    private final WebClient webClient;

    public GithubService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .build();
    }

    public Mono<Map<String, Object>> getUser(String accessToken) {
        return webClient.get()
                .uri("/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Flux<Map<String, Object>> getUserEmails(String accessToken) {
        return webClient.get()
                .uri("/user/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {});
    }

    public Mono<String> getPrimaryEmail(String accessToken) {
        Flux<Map<String, Object>> emails = getUserEmails(accessToken).cache();

        Mono<String> verifiedPrimary = emails.filter(email -> Boolean.TRUE.equals(email.get("primary")) && Boolean.TRUE.equals(email.get("verified")))
                .map(email -> (String) email.get("email"))
                .next();
        Mono<String> onlyPrimary = emails.filter(email -> Boolean.TRUE.equals(email.get("primary")))
                .map(email -> (String) email.get("email"))
                .next();
        return verifiedPrimary.switchIfEmpty(onlyPrimary);
    }
}
