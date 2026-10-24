package com.d1vivek.wallet.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.error.NotSignedInException;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.testing.FakeAuthRepository;
import com.d1vivek.wallet.testing.FakeWalletRepository;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public class GetAccountsUseCaseTest {

    private final FakeWalletRepository walletRepository = new FakeWalletRepository();
    private final FakeAuthRepository authRepository = FakeAuthRepository.signedIn();
    private final GetAccountsUseCase useCase = new GetAccountsUseCase(walletRepository, authRepository);

    @Test
    public void returnsAccountsFromTheRepositoryInOrder() throws Exception {
        Account usd = new Account("usd", "USD Account", Currency.getInstance("USD"), new BigDecimal("1.00"));
        Account eur = new Account("eur", "EUR Account", Currency.getInstance("EUR"), new BigDecimal("2.00"));
        walletRepository.accounts.add(usd);
        walletRepository.accounts.add(eur);

        List<Account> accounts = useCase.execute();

        assertEquals(List.of(usd, eur), accounts);
    }

    @Test
    public void returnsAnEmptyListWhenThereAreNoAccounts() throws Exception {
        assertTrue(useCase.execute().isEmpty());
    }

    @Test
    public void passesRepositoryFailuresThrough() {
        walletRepository.failing = true;

        assertThrows(DataUnavailableException.class, useCase::execute);
    }

    @Test
    public void refusesToLoadAfterSignOut() {
        authRepository.signOut();

        assertThrows(NotSignedInException.class, useCase::execute);
    }
}
