package com.d1vivek.wallet.presentation.wallet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.error.NotSignedInException;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.repository.AuthRepository;
import com.d1vivek.wallet.domain.usecase.GetAccountsUseCase;
import com.d1vivek.wallet.presentation.common.BackgroundTasks;
import com.d1vivek.wallet.presentation.common.UiState;

import java.util.List;

public class WalletViewModel extends ViewModel {

    private final GetAccountsUseCase getAccountsUseCase;
    private final AuthRepository authRepository;
    private final BackgroundTasks tasks = new BackgroundTasks();
    private final MutableLiveData<UiState<List<Account>>> state = new MutableLiveData<>();

    public WalletViewModel(GetAccountsUseCase getAccountsUseCase, AuthRepository authRepository) {
        this.getAccountsUseCase = getAccountsUseCase;
        this.authRepository = authRepository;
        load();
    }

    public LiveData<UiState<List<Account>>> getState() {
        return state;
    }

    public void retry() {
        load();
    }

    public void logout() {
        tasks.cancelPending();
        authRepository.signOut();
        state.setValue(UiState.error(UiState.Error.SIGNED_OUT));
    }

    private void load() {
        state.setValue(UiState.loading());
        tasks.run(this::fetchAccounts, UiState.error(UiState.Error.UNAVAILABLE), state::setValue);
    }

    private UiState<List<Account>> fetchAccounts() {
        try {
            List<Account> accounts = getAccountsUseCase.execute();
            return accounts.isEmpty() ? UiState.empty(accounts) : UiState.success(accounts);
        } catch (NotSignedInException e) {
            return UiState.error(UiState.Error.SIGNED_OUT);
        } catch (DataUnavailableException e) {
            return UiState.error(UiState.Error.UNAVAILABLE);
        }
    }

    @Override
    protected void onCleared() {
        tasks.shutdown();
    }
}
