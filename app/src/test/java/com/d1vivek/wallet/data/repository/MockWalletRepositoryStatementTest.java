package com.d1vivek.wallet.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.d1vivek.wallet.data.source.MockWalletDataSource;
import com.d1vivek.wallet.data.source.SimulatedLatency;
import com.d1vivek.wallet.domain.model.AccountStatement;
import com.d1vivek.wallet.domain.usecase.GetAccountStatementUseCase;
import com.d1vivek.wallet.testing.FakeAuthRepository;

import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public class MockWalletRepositoryStatementTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-23T12:00:00Z"), ZoneOffset.UTC);

    private final GetAccountStatementUseCase useCase = new GetAccountStatementUseCase(
            new MockWalletRepository(new MockWalletDataSource(CLOCK), new SimulatedLatency(0)),
            FakeAuthRepository.signedIn(),
            CLOCK);

    @Test
    public void usdStatementLeavesOutEntriesOlderThanThirtyDays() throws Exception {
        AccountStatement statement = useCase.execute(MockWalletDataSource.USD_ACCOUNT_ID);

        // 13 entries in the demo ledger; the two from 40+ days ago fall outside the window.
        assertEquals(11, statement.transactions().size());
        assertEquals("Monthly account fee", statement.transactions().get(0).getDescription());
    }

    @Test
    public void nprAccountShowsAnEmptyStatement() throws Exception {
        AccountStatement statement = useCase.execute(MockWalletDataSource.NPR_ACCOUNT_ID);

        assertTrue(statement.isEmpty());
        assertFalse(statement.account().getName().isEmpty());
    }
}
