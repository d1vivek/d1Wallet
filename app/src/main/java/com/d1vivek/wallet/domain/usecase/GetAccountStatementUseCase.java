package com.d1vivek.wallet.domain.usecase;

import com.d1vivek.wallet.domain.error.AccountNotFoundException;
import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.error.NotSignedInException;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.model.AccountStatement;
import com.d1vivek.wallet.domain.model.Transaction;
import com.d1vivek.wallet.domain.repository.AuthRepository;
import com.d1vivek.wallet.domain.repository.WalletRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GetAccountStatementUseCase {

    public static final Duration STATEMENT_PERIOD = Duration.ofDays(30);

    static final Comparator<Transaction> NEWEST_FIRST =
            Comparator.comparing(Transaction::getTimestamp)
                    .thenComparing(Transaction::getId)
                    .reversed();

    private final WalletRepository walletRepository;
    private final AuthRepository authRepository;
    private final Clock clock;

    public GetAccountStatementUseCase(
            WalletRepository walletRepository, AuthRepository authRepository, Clock clock) {
        this.walletRepository = walletRepository;
        this.authRepository = authRepository;
        this.clock = clock;
    }

    public AccountStatement execute(String accountId)
            throws NotSignedInException, AccountNotFoundException, DataUnavailableException {
        if (!authRepository.isSignedIn()) {
            throw new NotSignedInException();
        }
        if (accountId == null || accountId.isEmpty()) {
            throw new AccountNotFoundException(String.valueOf(accountId));
        }

        Account account = walletRepository.findAccount(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        Instant to = clock.instant();
        Instant from = to.minus(STATEMENT_PERIOD);

        List<Transaction> inPeriod = new ArrayList<>();
        for (Transaction transaction : walletRepository.getTransactions(accountId)) {
            Instant timestamp = transaction.getTimestamp();
            boolean belongsToAccount = accountId.equals(transaction.getAccountId());
            boolean inWindow = !timestamp.isBefore(from) && !timestamp.isAfter(to);
            if (belongsToAccount && inWindow) {
                inPeriod.add(transaction);
            }
        }
        inPeriod.sort(NEWEST_FIRST);

        return new AccountStatement(account, from, to, inPeriod);
    }
}
