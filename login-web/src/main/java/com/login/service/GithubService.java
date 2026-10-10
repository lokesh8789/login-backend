package com.login.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GithubService {
    private final RestClient restClient;

    public GithubService() {
        restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .build();
    }

    public Map<String, Object> getUser(String accessToken) {
        return restClient.get()
                .uri("/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<Map<String, Object>> getUserEmails(String accessToken) {
        return restClient.get()
                .uri("/user/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public String getPrimaryEmail(String accessToken) {
        List<Map<String, Object>> emails = getUserEmails(accessToken);
        if (emails == null || emails.isEmpty()) {
            return null;
        }
        return emails.stream()
                .filter(email -> Boolean.TRUE.equals(email.get("primary")) && Boolean.TRUE.equals(email.get("verified")))
                .map(email -> (String) email.get("email"))
                .findFirst()
                .orElseGet(() -> emails.stream()
                        .filter(email -> Boolean.TRUE.equals(email.get("primary")))
                        .map(email -> (String) email.get("email"))
                        .findFirst()
                        .orElse(null));
    }
}
