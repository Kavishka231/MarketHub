package com.markethub.auth;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException() {
        super("Account is disabled");
    }
}
