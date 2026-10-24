package com.d1vivek.wallet.data.repository;

import com.d1vivek.wallet.data.source.MockWalletDataSource;
import com.d1vivek.wallet.data.source.SimulatedLatency;
import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.model.Transaction;
import com.d1vivek.wallet.domain.repository.WalletRepository;

import java.util.List;
import java.util.Optional;

public class MockWalletRepository implements WalletRepository {

    private final MockWalletDataSource dataSource;
    private final SimulatedLatency latency;

    public MockWalletRepository(MockWalletDataSource dataSource, SimulatedLatency latency) {
        this.dataSource = dataSource;
        this.latency = latency;
    }

    @Override
    public List<Account> getAccounts() throws DataUnavailableException {
        latency.pause();
        return dataSource.getAccounts();
    }

    @Override
    public Optional<Account> findAccount(String accountId) throws DataUnavailableException {
        latency.pause();
        for (Account account : dataSource.getAccounts()) {
            if (account.getId().equals(accountId)) {
                return Optional.of(account);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> getTransactions(String accountId) throws DataUnavailableException {
        latency.pause();
        return dataSource.getTransactions(accountId);
    }
}
