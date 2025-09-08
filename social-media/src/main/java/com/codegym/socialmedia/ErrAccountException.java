package com.codegym.socialmedia;

import org.springframework.security.core.AuthenticationException;

public class ErrAccountException extends AuthenticationException {
    public ErrAccountException(String msg) {
        super(msg);
    }
}
