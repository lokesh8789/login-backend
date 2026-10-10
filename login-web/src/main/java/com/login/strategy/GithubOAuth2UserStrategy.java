package com.login.strategy;

import com.login.dto.OAuth2UserInfo;
import com.login.enums.AuthProvider;
import com.login.service.GithubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GithubOAuth2UserStrategy implements OAuth2UserStrategy {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GithubService githubService;

    @Override
    public AuthProvider getKey() {
        return AuthProvider.GITHUB;
    }

    @Override
    public OAuth2UserInfo extract(OAuth2AuthenticationToken authenticationToken) {
        OAuth2User principal = authenticationToken.getPrincipal();

        Object id = principal.getAttribute("id");
        String providerId = id != null ? id.toString(): null;

        String name = principal.getAttribute("name");
        if (name == null || name.isBlank()) {
            name = principal.getAttribute("login");
        }
        String email = principal.getAttribute("email");
        if (email == null || email.isBlank()) {
            email = fetchPrimaryEmail(authenticationToken);
        }
        log.info("providerId: {}, name: {}, email: {}", providerId, name, email);
        return OAuth2UserInfo.builder()
                .authProvider(getKey())
                .providerId(providerId)
                .email(email)
                .name(name)
                .build();
    }

    private String fetchPrimaryEmail(OAuth2AuthenticationToken token) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token.getName());
            if (client != null && client.getAccessToken() != null) {
                return githubService.getPrimaryEmail(client.getAccessToken().getTokenValue());
            }
        } catch (Exception e) {
            log.info("Failed to fetch primary email for GitHub user: {}", e.getMessage());
        }
        return null;
    }
}
