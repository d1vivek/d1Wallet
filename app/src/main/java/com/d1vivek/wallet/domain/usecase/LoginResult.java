package com.d1vivek.wallet.domain.usecase;

public final class LoginResult {

    public enum Status { SUCCESS, MISSING_FIELDS, INVALID_CREDENTIALS, UNAVAILABLE }

    private static final LoginResult SUCCESS = new LoginResult(Status.SUCCESS, false, false);
    private static final LoginResult INVALID = new LoginResult(Status.INVALID_CREDENTIALS, false, false);
    private static final LoginResult UNAVAILABLE = new LoginResult(Status.UNAVAILABLE, false, false);

    private final Status status;
    private final boolean identifierMissing;
    private final boolean passwordMissing;

    private LoginResult(Status status, boolean identifierMissing, boolean passwordMissing) {
        this.status = status;
        this.identifierMissing = identifierMissing;
        this.passwordMissing = passwordMissing;
    }

    public static LoginResult success() {
        return SUCCESS;
    }

    public static LoginResult invalidCredentials() {
        return INVALID;
    }

    public static LoginResult unavailable() {
        return UNAVAILABLE;
    }

    public static LoginResult missingFields(boolean identifierMissing, boolean passwordMissing) {
        return new LoginResult(Status.MISSING_FIELDS, identifierMissing, passwordMissing);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isIdentifierMissing() {
        return identifierMissing;
    }

    public boolean isPasswordMissing() {
        return passwordMissing;
    }
}
