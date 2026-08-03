package com.fellowlodge.api.common.exception;

import org.springframework.http.HttpStatus;

public class AccountDisabledException extends BusinessException {

    public AccountDisabledException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
