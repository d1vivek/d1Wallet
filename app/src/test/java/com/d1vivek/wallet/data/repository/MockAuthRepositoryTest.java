package com.d1vivek.wallet.data.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.d1vivek.wallet.data.source.SimulatedLatency;

import org.junit.Test;

public class MockAuthRepositoryTest {

    private final MockAuthRepository repository = new MockAuthRepository(new SimulatedLatency(0));

    @Test
    public void startsSignedOut() {
        assertFalse(repository.isSignedIn());
    }

    @Test
    public void signOutEndsTheSession() throws Exception {
        repository.signIn(MockAuthRepository.DEMO_IDENTIFIER, MockAuthRepository.DEMO_PASSWORD);
        assertTrue(repository.isSignedIn());

        repository.signOut();

        assertFalse(repository.isSignedIn());
    }

    @Test
    public void failedAttemptEndsAnExistingSession() throws Exception {
        repository.signIn(MockAuthRepository.DEMO_IDENTIFIER, MockAuthRepository.DEMO_PASSWORD);

        repository.signIn(MockAuthRepository.DEMO_IDENTIFIER, "wrong");

        assertFalse(repository.isSignedIn());
    }
}
