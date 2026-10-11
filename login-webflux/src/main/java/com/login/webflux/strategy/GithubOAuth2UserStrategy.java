package com.login.webflux.strategy;

import com.login.webflux.dto.OAuth2UserInfo;
import com.login.webflux.enums.AuthProvider;
import com.login.webflux.service.GithubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class GithubOAuth2UserStrategy implements OAuth2UserStrategy {

    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
    private final GithubService githubService;

    @Override
    public AuthProvider getKey() {
        return AuthProvider.GITHUB;
    }

    @Override
    public Mono<OAuth2UserInfo> extract(OAuth2AuthenticationToken authenticationToken) {
        OAuth2User principal = authenticationToken.getPrincipal();

        Object id = principal.getAttribute("id");
        String providerId = id != null ? id.toString(): null;

        String name = principal.getAttribute("name");
        if (name == null || name.isBlank()) {
            name = principal.getAttribute("login");
        }
        String email = principal.getAttribute("email");
        OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                .authProvider(getKey())
                .providerId(providerId)
                .name(name)
                .build();
        return Mono.justOrEmpty(email)
                .filter(s -> !s.isBlank())
                .switchIfEmpty(fetchPrimaryEmail(authenticationToken))
                .doOnNext(mail -> log.info("providerId: {}, name: {}, email: {}", userInfo.getProviderId(), userInfo.getName(), mail))
                .map(mail -> userInfo.toBuilder().email(mail).build())
                .defaultIfEmpty(userInfo);
    }

    private Mono<String> fetchPrimaryEmail(OAuth2AuthenticationToken token) {
        return authorizedClientService.loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token.getName())
                .filter(Objects::nonNull)
                .mapNotNull(OAuth2AuthorizedClient::getAccessToken)
                .mapNotNull(AbstractOAuth2Token::getTokenValue)
                .flatMap(githubService::getPrimaryEmail)
                .onErrorResume(e -> {
                    log.info("Failed to fetch primary email for GitHub user: {}", e.getMessage());
                    return Mono.empty();
                });
    }
}
