package com.login.webflux.strategy;

public interface KeyedStrategy<K> {
    K getKey();
}
