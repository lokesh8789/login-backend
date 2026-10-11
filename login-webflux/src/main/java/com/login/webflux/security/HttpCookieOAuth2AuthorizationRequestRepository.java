package com.login.webflux.security;

import com.login.webflux.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository implements ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public Mono<OAuth2AuthorizationRequest> loadAuthorizationRequest(ServerWebExchange exchange) {
        return Mono.fromCallable(() ->
                        CookieUtils.getCookie(exchange, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                                .orElse(null))
                .onErrorResume(e -> {
                    log.error("Failed to deserialize OAuth2 authorization request cookie: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, ServerWebExchange exchange) {
        return Mono.fromRunnable(() -> {
            if (authorizationRequest == null) {
                CookieUtils.deleteCookie(exchange, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
                return;
            }
            CookieUtils.addCookie(
                    exchange,
                    OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                    CookieUtils.serialize(authorizationRequest),
                    COOKIE_EXPIRE_SECONDS
            );
        });
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> removeAuthorizationRequest(ServerWebExchange exchange) {
        return loadAuthorizationRequest(exchange)
                .doOnNext(req -> CookieUtils.deleteCookie(exchange, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME));
    }
}
