package com.d1vivek.wallet.domain.usecase;

import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.error.NotSignedInException;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.repository.AuthRepository;
import com.d1vivek.wallet.domain.repository.WalletRepository;

import java.util.List;

public class GetAccountsUseCase {

    private final WalletRepository walletRepository;
    private final AuthRepository authRepository;

    public GetAccountsUseCase(WalletRepository walletRepository, AuthRepository authRepository) {
        this.walletRepository = walletRepository;
        this.authRepository = authRepository;
    }

    public List<Account> execute() throws NotSignedInException, DataUnavailableException {
        if (!authRepository.isSignedIn()) {
            throw new NotSignedInException();
        }
        return walletRepository.getAccounts();
    }
}
