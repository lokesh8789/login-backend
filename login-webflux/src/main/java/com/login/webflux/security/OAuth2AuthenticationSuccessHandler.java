package com.login.webflux.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.login.webflux.dto.JwtAuthResponse;
import com.login.webflux.enums.ErrorCode;
import com.login.webflux.exceptions.StrategyNotFoundException;
import com.login.webflux.factory.OAuth2UserStrategyFactory;
import com.login.webflux.service.UserService;
import com.login.webflux.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
    private final OAuth2UserStrategyFactory oAuth2UserStrategyFactory;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();
    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
        ServerWebExchange exchange = webFilterExchange.getExchange();
        return Mono.fromCallable(() -> oAuth2UserStrategyFactory.getStrategy(authenticationToken.getAuthorizedClientRegistrationId()))
                .flatMap(strategy -> strategy.extract(authenticationToken))
                .flatMap(userInfo -> {
                    if (userInfo.getProviderId() == null || userInfo.getProviderId().isBlank()) {
                        return redirectWithError(exchange, ErrorCode.INVALID_PROVIDER_RESPONSE.getValue());
                    }
                    if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
                        return redirectWithError(exchange, ErrorCode.EMAIL_NOT_PROVIDED.getValue());
                    }
                    return userService.processOAuthUser(userInfo)
                            .flatMap(user -> {
                                String token = jwtTokenUtil.generateToken(user.getEmail(), Map.of("userId", user.getId().toString()));
                                Date expiration = jwtTokenUtil.extractExpiration(token);
                                return Mono.fromCallable(() -> objectMapper.writeValueAsString(new JwtAuthResponse(user, token, new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(expiration))))
                                        .map(data -> Base64.getUrlEncoder().encodeToString(data.getBytes()))
                                        .flatMap(encodedData -> redirectStrategy.sendRedirect(exchange, URI.create(Constants.FRONTEND_OAUTH_SUCCESS_URL + "?data=" + encodedData)));
                            });
                })
                .onErrorResume(StrategyNotFoundException.class, e -> {
                    log.error("Unsupported OAuth provider: ", e);
                    return redirectWithError(exchange, ErrorCode.UNSUPPORTED_PROVIDER.getValue());
                })
                .onErrorResume(e -> {
                    log.error("OAuth authentication error: ", e);
                    return redirectWithError(exchange, ErrorCode.INTERNAL_SERVER_ERROR.getValue());
                });
    }

    private Mono<Void> redirectWithError(ServerWebExchange webExchange, String error) {
        return redirectStrategy.sendRedirect(webExchange, URI.create(Constants.FRONTEND_OAUTH_FAILURE_URL + "?error=" + error));
    }
}
