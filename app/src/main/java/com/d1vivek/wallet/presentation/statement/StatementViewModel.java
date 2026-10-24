package com.d1vivek.wallet.presentation.statement;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.d1vivek.wallet.domain.error.AccountNotFoundException;
import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.error.NotSignedInException;
import com.d1vivek.wallet.domain.model.AccountStatement;
import com.d1vivek.wallet.domain.usecase.GetAccountStatementUseCase;
import com.d1vivek.wallet.presentation.common.BackgroundTasks;
import com.d1vivek.wallet.presentation.common.UiState;

public class StatementViewModel extends ViewModel {

    private final GetAccountStatementUseCase getStatementUseCase;
    @Nullable private final String accountId;
    private final BackgroundTasks tasks = new BackgroundTasks();
    private final MutableLiveData<UiState<AccountStatement>> state = new MutableLiveData<>();

    public StatementViewModel(GetAccountStatementUseCase getStatementUseCase, @Nullable String accountId) {
        this.getStatementUseCase = getStatementUseCase;
        this.accountId = accountId;
        load();
    }

    public LiveData<UiState<AccountStatement>> getState() {
        return state;
    }

    public void retry() {
        load();
    }

    private void load() {
        if (accountId == null || accountId.isEmpty()) {
            state.setValue(UiState.error(UiState.Error.NOT_FOUND));
            return;
        }
        state.setValue(UiState.loading());
        tasks.run(this::fetchStatement, UiState.error(UiState.Error.UNAVAILABLE), state::setValue);
    }

    private UiState<AccountStatement> fetchStatement() {
        try {
            AccountStatement statement = getStatementUseCase.execute(accountId);
            return statement.isEmpty() ? UiState.empty(statement) : UiState.success(statement);
        } catch (NotSignedInException e) {
            return UiState.error(UiState.Error.SIGNED_OUT);
        } catch (AccountNotFoundException e) {
            return UiState.error(UiState.Error.NOT_FOUND);
        } catch (DataUnavailableException e) {
            return UiState.error(UiState.Error.UNAVAILABLE);
        }
    }

    @Override
    protected void onCleared() {
        tasks.shutdown();
    }
}
