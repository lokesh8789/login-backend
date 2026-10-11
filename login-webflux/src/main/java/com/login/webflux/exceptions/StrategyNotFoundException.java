package com.login.webflux.exceptions;

public class StrategyNotFoundException extends RuntimeException {
    public StrategyNotFoundException(String message) {
        super(message);
    }
}
