package com.d1vivek.wallet.testing;

import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.model.Transaction;
import com.d1vivek.wallet.domain.repository.WalletRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeWalletRepository implements WalletRepository {

    public final List<Account> accounts = new ArrayList<>();
    public final List<Transaction> transactions = new ArrayList<>();
    public boolean failing;

    @Override
    public List<Account> getAccounts() throws DataUnavailableException {
        throwIfFailing();
        return new ArrayList<>(accounts);
    }

    @Override
    public Optional<Account> findAccount(String accountId) throws DataUnavailableException {
        throwIfFailing();
        return accounts.stream().filter(a -> a.getId().equals(accountId)).findFirst();
    }

    /** Returns every stored transaction, on purpose, so tests can check the use case filters by account. */
    @Override
    public List<Transaction> getTransactions(String accountId) throws DataUnavailableException {
        throwIfFailing();
        return new ArrayList<>(transactions);
    }

    private void throwIfFailing() throws DataUnavailableException {
        if (failing) {
            throw new DataUnavailableException("simulated outage");
        }
    }
}
