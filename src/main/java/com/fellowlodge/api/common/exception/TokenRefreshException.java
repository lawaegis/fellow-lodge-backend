package com.fellowlodge.api.common.exception;

import org.springframework.http.HttpStatus;

public class TokenRefreshException extends BusinessException {

    public TokenRefreshException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
