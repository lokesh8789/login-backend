package com.login.strategy;

import com.login.dto.OAuth2UserInfo;
import com.login.enums.AuthProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public interface OAuth2UserStrategy extends KeyedStrategy<AuthProvider> {
    OAuth2UserInfo extract(OAuth2AuthenticationToken authenticationToken);
}
