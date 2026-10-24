package com.d1vivek.wallet.domain.repository;

import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface WalletRepository {

    List<Account> getAccounts() throws DataUnavailableException;

    Optional<Account> findAccount(String accountId) throws DataUnavailableException;

    List<Transaction> getTransactions(String accountId) throws DataUnavailableException;
}
