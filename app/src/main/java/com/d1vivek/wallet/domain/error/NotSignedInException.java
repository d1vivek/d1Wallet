package com.d1vivek.wallet.domain.error;

public class NotSignedInException extends Exception {

    public NotSignedInException() {
        super("No active session");
    }
}
