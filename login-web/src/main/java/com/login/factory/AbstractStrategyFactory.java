package com.login.factory;

import com.login.exceptions.StrategyNotFoundException;
import com.login.strategy.KeyedStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractStrategyFactory<K, S extends KeyedStrategy<K>> {
    private final Map<K, S> strategyMap;

    protected AbstractStrategyFactory(List<S> strategyList) {
        this.strategyMap = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(KeyedStrategy::getKey, Function.identity()));
    }

    public S getStrategy(K key) {
        if (key == null) {
            throw new StrategyNotFoundException("Strategy key cannot be null");
        }
        S strategy = strategyMap.get(key);
        if (strategy == null) {
            throw new StrategyNotFoundException("No Strategy Found For: " + key);
        }
        log.info("Strategy Found: {} ", strategy.getClass().getName());
        return strategy;
    }
}
