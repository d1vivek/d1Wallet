package com.d1vivek.wallet.presentation.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.d1vivek.wallet.domain.repository.AuthRepository;
import com.d1vivek.wallet.domain.usecase.LoginResult;
import com.d1vivek.wallet.domain.usecase.LoginUseCase;
import com.d1vivek.wallet.presentation.common.BackgroundTasks;

public class LoginViewModel extends ViewModel {

    private final LoginUseCase loginUseCase;
    private final BackgroundTasks tasks = new BackgroundTasks();
    private final MutableLiveData<LoginUiState> state;

    public LoginViewModel(LoginUseCase loginUseCase, AuthRepository authRepository) {
        this.loginUseCase = loginUseCase;
        this.state = new MutableLiveData<>(
                authRepository.isSignedIn() ? LoginUiState.signedIn() : LoginUiState.idle());
    }

    public LiveData<LoginUiState> getState() {
        return state;
    }

    public void submit(String identifier, String password) {
        LoginUiState current = currentState();
        if (current.isLoading() || current.isSignedIn()) {
            return;
        }
        state.setValue(LoginUiState.loading());
        tasks.run(
                () -> loginUseCase.execute(identifier, password),
                LoginResult.unavailable(),
                result -> state.setValue(toUiState(result)));
    }

    public void onIdentifierEdited() {
        LoginUiState current = currentState();
        if (current.isIdentifierMissing() || current.getFormError() != LoginUiState.FormError.NONE) {
            state.setValue(current.withoutIdentifierError());
        }
    }

    public void onPasswordEdited() {
        LoginUiState current = currentState();
        if (current.isPasswordMissing() || current.getFormError() != LoginUiState.FormError.NONE) {
            state.setValue(current.withoutPasswordError());
        }
    }

    private LoginUiState currentState() {
        LoginUiState current = state.getValue();
        return current == null ? LoginUiState.idle() : current;
    }

    private static LoginUiState toUiState(LoginResult result) {
        return switch (result.getStatus()) {
            case SUCCESS -> LoginUiState.signedIn();
            case MISSING_FIELDS ->
                    LoginUiState.missingFields(result.isIdentifierMissing(), result.isPasswordMissing());
            case INVALID_CREDENTIALS ->
                    LoginUiState.failed(LoginUiState.FormError.INVALID_CREDENTIALS);
            default -> LoginUiState.failed(LoginUiState.FormError.UNAVAILABLE);
        };
    }

    @Override
    protected void onCleared() {
        tasks.shutdown();
    }
}
