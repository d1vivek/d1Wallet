package com.d1vivek.wallet.app;

import com.d1vivek.wallet.data.repository.MockAuthRepository;
import com.d1vivek.wallet.data.repository.MockWalletRepository;
import com.d1vivek.wallet.data.source.MockWalletDataSource;
import com.d1vivek.wallet.data.source.SimulatedLatency;
import com.d1vivek.wallet.domain.repository.AuthRepository;
import com.d1vivek.wallet.domain.repository.WalletRepository;
import com.d1vivek.wallet.domain.usecase.GetAccountStatementUseCase;
import com.d1vivek.wallet.domain.usecase.GetAccountsUseCase;
import com.d1vivek.wallet.domain.usecase.LoginUseCase;
import com.d1vivek.wallet.presentation.common.ViewModelFactory;
import com.d1vivek.wallet.presentation.login.LoginViewModel;
import com.d1vivek.wallet.presentation.statement.StatementViewModel;
import com.d1vivek.wallet.presentation.wallet.WalletViewModel;

import java.time.Clock;

/**
 * Wires the app together. This is the only place that knows which repository implementations are
 * in use, so swapping the mocks for real ones means changing this class and nothing else.

 * TODO >> later this can be changes with some DI
 */
public final class AppContainer {

    private static final long AUTH_DELAY_MILLIS = 900;
    private static final long WALLET_DELAY_MILLIS = 400;

    private final AuthRepository authRepository;
    private final WalletRepository walletRepository;
    private final Clock clock;

    AppContainer(Clock clock) {
        this.clock = clock;
        this.authRepository = new MockAuthRepository(new SimulatedLatency(AUTH_DELAY_MILLIS));
        this.walletRepository = new MockWalletRepository(
                new MockWalletDataSource(clock), new SimulatedLatency(WALLET_DELAY_MILLIS));
    }

    public ViewModelFactory<LoginViewModel> loginViewModelFactory() {
        return new ViewModelFactory<>(LoginViewModel.class,
                () -> new LoginViewModel(new LoginUseCase(authRepository), authRepository));
    }

    public ViewModelFactory<WalletViewModel> walletViewModelFactory() {
        return new ViewModelFactory<>(WalletViewModel.class,
                () -> new WalletViewModel(
                        new GetAccountsUseCase(walletRepository, authRepository), authRepository));
    }

    public ViewModelFactory<StatementViewModel> statementViewModelFactory(String accountId) {
        return new ViewModelFactory<>(StatementViewModel.class,
                () -> new StatementViewModel(
                        new GetAccountStatementUseCase(walletRepository, authRepository, clock),
                        accountId));
    }
}
