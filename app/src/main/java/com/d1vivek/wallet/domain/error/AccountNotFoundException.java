package com.d1vivek.wallet.domain.error;

public class AccountNotFoundException extends Exception {

    public AccountNotFoundException(String accountId) {
        super("No account with id " + accountId);
    }
}
