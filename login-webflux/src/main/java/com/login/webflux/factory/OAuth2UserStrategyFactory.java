package com.login.webflux.factory;

import com.login.webflux.enums.AuthProvider;
import com.login.webflux.exceptions.StrategyNotFoundException;
import com.login.webflux.strategy.OAuth2UserStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OAuth2UserStrategyFactory extends AbstractStrategyFactory<AuthProvider, OAuth2UserStrategy> {

    public OAuth2UserStrategyFactory(List<OAuth2UserStrategy> strategyList) {
        super(strategyList);
    }

    public OAuth2UserStrategy getStrategy(String registrationId) {
        AuthProvider provider = AuthProvider.fromString(registrationId)
                .orElseThrow(() -> new StrategyNotFoundException("Unsupported OAuth provider: " + registrationId));
        return super.getStrategy(provider);
    }
}
