package com.login.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_PROVIDER_RESPONSE("invalid_provider_response"),
    EMAIL_NOT_PROVIDED("email_not_provided"),
    UNSUPPORTED_PROVIDER("unsupported_provider"),
    USER_NOT_FOUND("user_not_found"),
    USER_ALREADY_EXISTS("user_already_exists"),
    INVALID_CREDENTIALS("invalid_credentials"),
    AUTHENTICATION_FAILED("authentication_failed"),
    INTERNAL_SERVER_ERROR("internal_server_error");

    private final String value;
}
