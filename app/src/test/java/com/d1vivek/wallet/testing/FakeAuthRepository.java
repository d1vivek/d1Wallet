package com.d1vivek.wallet.testing;

import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.repository.AuthRepository;

public class FakeAuthRepository implements AuthRepository {

    public boolean signedIn;
    public boolean acceptCredentials;
    public boolean failNextSignIn;
    public int signInCalls;

    public static FakeAuthRepository signedIn() {
        FakeAuthRepository fake = new FakeAuthRepository();
        fake.signedIn = true;
        return fake;
    }

    @Override
    public boolean signIn(String identifier, String password) throws DataUnavailableException {
        signInCalls++;
        if (failNextSignIn) {
            throw new DataUnavailableException("simulated outage");
        }
        signedIn = acceptCredentials;
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
