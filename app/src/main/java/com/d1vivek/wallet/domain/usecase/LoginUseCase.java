package com.d1vivek.wallet.domain.usecase;

import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.repository.AuthRepository;

public class LoginUseCase {

    private final AuthRepository authRepository;

    public LoginUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LoginResult execute(String identifier, String password) {
        String trimmedIdentifier = identifier == null ? "" : identifier.trim();
        boolean identifierMissing = trimmedIdentifier.isEmpty();
        boolean passwordMissing = password == null || password.isEmpty();
        if (identifierMissing || passwordMissing) {
            return LoginResult.missingFields(identifierMissing, passwordMissing);
        }

        try {
            return authRepository.signIn(trimmedIdentifier, password)
                    ? LoginResult.success()
                    : LoginResult.invalidCredentials();
        } catch (DataUnavailableException e) {
            return LoginResult.unavailable();
        }
    }
}
