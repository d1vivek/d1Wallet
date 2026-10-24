package com.d1vivek.wallet.data.repository;

import com.d1vivek.wallet.data.source.SimulatedLatency;
import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.repository.AuthRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * mock auth repo for coding exercise
 */
public class MockAuthRepository implements AuthRepository {

    static final String DEMO_IDENTIFIER = "demo@example.com";
    static final String DEMO_PASSWORD = "password123";

    private final SimulatedLatency latency;
    private volatile boolean signedIn;

    public MockAuthRepository(SimulatedLatency latency) {
        this.latency = latency;
    }

    @Override
    public boolean signIn(String identifier, String password) throws DataUnavailableException {
        latency.pause();
        boolean identifierMatches = identifier != null
                && DEMO_IDENTIFIER.equals(identifier.trim().toLowerCase(Locale.ROOT));
        boolean passwordMatches = password != null && MessageDigest.isEqual(
                DEMO_PASSWORD.getBytes(StandardCharsets.UTF_8),
                password.getBytes(StandardCharsets.UTF_8));
        signedIn = identifierMatches && passwordMatches;
        return signedIn;
    }

    @Override
    public void signOut() {
        signedIn = false;
    }

    @Override
    public boolean isSignedIn() {
        return signedIn;
    }
}
