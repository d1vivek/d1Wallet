package com.d1vivek.wallet.domain.error;

public class DataUnavailableException extends Exception {

    public DataUnavailableException(String message) {
        super(message);
    }

    public DataUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
