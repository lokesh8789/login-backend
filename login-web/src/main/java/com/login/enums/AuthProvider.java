package com.login.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum AuthProvider {
    GITHUB,
    GOOGLE;

    private static final Map<String, AuthProvider> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    p -> p.name().toUpperCase(),
                    Function.identity()
            ));

    public static Optional<AuthProvider> fromString(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(LOOKUP.get(providerName.trim().toUpperCase()));
    }
}
