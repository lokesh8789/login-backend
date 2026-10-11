package com.login.webflux.strategy;

import com.login.webflux.dto.OAuth2UserInfo;
import com.login.webflux.enums.AuthProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Mono;

public interface OAuth2UserStrategy extends KeyedStrategy<AuthProvider> {
    Mono<OAuth2UserInfo> extract(OAuth2AuthenticationToken authenticationToken);
}
