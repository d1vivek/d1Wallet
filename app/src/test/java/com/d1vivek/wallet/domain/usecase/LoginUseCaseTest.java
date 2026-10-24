package com.d1vivek.wallet.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.d1vivek.wallet.data.repository.MockAuthRepository;
import com.d1vivek.wallet.data.source.SimulatedLatency;
import com.d1vivek.wallet.testing.FakeAuthRepository;

import org.junit.Before;
import org.junit.Test;

public class LoginUseCaseTest {

    private MockAuthRepository authRepository;
    private LoginUseCase loginUseCase;

    @Before
    public void setUp() {
        authRepository = new MockAuthRepository(new SimulatedLatency(0));
        loginUseCase = new LoginUseCase(authRepository);
    }

    @Test
    public void demoCredentialsSignTheUserIn() {
        LoginResult result = loginUseCase.execute("demo@example.com", "password123");

        assertEquals(LoginResult.Status.SUCCESS, result.getStatus());
        assertTrue(authRepository.isSignedIn());
    }

    @Test
    public void emailIsMatchedIgnoringCaseAndSurroundingSpaces() {
        LoginResult result = loginUseCase.execute("  Demo@Example.COM ", "password123");

        assertEquals(LoginResult.Status.SUCCESS, result.getStatus());
    }

    @Test
    public void wrongPasswordIsRejected() {
        LoginResult result = loginUseCase.execute("demo@example.com", "password124");

        assertEquals(LoginResult.Status.INVALID_CREDENTIALS, result.getStatus());
        assertFalse(authRepository.isSignedIn());
    }

    @Test
    public void passwordIsCaseSensitive() {
        LoginResult result = loginUseCase.execute("demo@example.com", "PASSWORD123");

        assertEquals(LoginResult.Status.INVALID_CREDENTIALS, result.getStatus());
    }

    @Test
    public void unknownUserIsRejected() {
        LoginResult result = loginUseCase.execute("someone@example.com", "password123");

        assertEquals(LoginResult.Status.INVALID_CREDENTIALS, result.getStatus());
        assertFalse(authRepository.isSignedIn());
    }

    @Test
    public void emptyIdentifierIsReportedWithoutCallingTheRepository() {
        FakeAuthRepository fake = new FakeAuthRepository();

        LoginResult result = new LoginUseCase(fake).execute("", "password123");

        assertEquals(LoginResult.Status.MISSING_FIELDS, result.getStatus());
        assertTrue(result.isIdentifierMissing());
        assertFalse(result.isPasswordMissing());
        assertEquals(0, fake.signInCalls);
    }

    @Test
    public void whitespaceOnlyIdentifierCountsAsEmpty() {
        LoginResult result = loginUseCase.execute("   ", "password123");

        assertEquals(LoginResult.Status.MISSING_FIELDS, result.getStatus());
        assertTrue(result.isIdentifierMissing());
    }

    @Test
    public void emptyPasswordIsReported() {
        LoginResult result = loginUseCase.execute("demo@example.com", "");

        assertEquals(LoginResult.Status.MISSING_FIELDS, result.getStatus());
        assertFalse(result.isIdentifierMissing());
        assertTrue(result.isPasswordMissing());
    }

    @Test
    public void bothFieldsEmptyAreReportedTogether() {
        LoginResult result = loginUseCase.execute(null, null);

        assertTrue(result.isIdentifierMissing());
        assertTrue(result.isPasswordMissing());
    }

    @Test
    public void repositoryFailureBecomesUnavailableInsteadOfAnException() {
        FakeAuthRepository fake = new FakeAuthRepository();
        fake.failNextSignIn = true;

        LoginResult result = new LoginUseCase(fake).execute("demo@example.com", "password123");

        assertEquals(LoginResult.Status.UNAVAILABLE, result.getStatus());
        assertFalse(fake.isSignedIn());
    }
}
