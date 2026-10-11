package com.login.webflux.repo;

import com.login.webflux.entity.UserProvider;
import com.login.webflux.enums.AuthProvider;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserProviderRepo extends R2dbcRepository<UserProvider, Integer> {
    Mono<UserProvider> findByProviderIdAndProvider(String providerId, AuthProvider provider);
}
