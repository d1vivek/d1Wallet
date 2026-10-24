package com.d1vivek.wallet.domain.repository;

import com.d1vivek.wallet.domain.error.DataUnavailableException;

public interface AuthRepository {

    /**
     * Checks the credentials and starts a session if they match. May block, so call it off the
     * main thread.
     *
     * @return {@code true} if the user is now signed in, {@code false} if the credentials were wrong
     * @throws DataUnavailableException if the check couldn't be completed
     */
    boolean signIn(String identifier, String password) throws DataUnavailableException;

    void signOut();

    boolean isSignedIn();
}
