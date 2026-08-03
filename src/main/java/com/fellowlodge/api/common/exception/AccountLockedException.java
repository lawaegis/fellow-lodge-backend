package com.fellowlodge.api.common.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends BusinessException {

    public AccountLockedException(String message) {
        super(message, HttpStatus.LOCKED);
    }
}
