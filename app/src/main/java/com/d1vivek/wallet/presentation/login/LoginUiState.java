package com.d1vivek.wallet.presentation.login;

import androidx.annotation.NonNull;

public final class LoginUiState {

    public enum FormError { NONE, INVALID_CREDENTIALS, UNAVAILABLE }

    private final boolean loading;
    private final boolean identifierMissing;
    private final boolean passwordMissing;
    @NonNull private final FormError formError;
    private final boolean signedIn;

    private LoginUiState(boolean loading, boolean identifierMissing, boolean passwordMissing,
                         @NonNull FormError formError, boolean signedIn) {
        this.loading = loading;
        this.identifierMissing = identifierMissing;
        this.passwordMissing = passwordMissing;
        this.formError = formError;
        this.signedIn = signedIn;
    }

    static LoginUiState idle() {
        return new LoginUiState(false, false, false, FormError.NONE, false);
    }

    static LoginUiState loading() {
        return new LoginUiState(true, false, false, FormError.NONE, false);
    }

    static LoginUiState missingFields(boolean identifierMissing, boolean passwordMissing) {
        return new LoginUiState(false, identifierMissing, passwordMissing, FormError.NONE, false);
    }

    static LoginUiState failed(@NonNull FormError formError) {
        return new LoginUiState(false, false, false, formError, false);
    }

    static LoginUiState signedIn() {
        return new LoginUiState(false, false, false, FormError.NONE, true);
    }

    LoginUiState withoutIdentifierError() {
        return new LoginUiState(loading, false, passwordMissing, FormError.NONE, signedIn);
    }

    LoginUiState withoutPasswordError() {
        return new LoginUiState(loading, identifierMissing, false, FormError.NONE, signedIn);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isIdentifierMissing() {
        return identifierMissing;
    }

    public boolean isPasswordMissing() {
        return passwordMissing;
    }

    @NonNull
    public FormError getFormError() {
        return formError;
    }

    public boolean isSignedIn() {
        return signedIn;
    }
}
