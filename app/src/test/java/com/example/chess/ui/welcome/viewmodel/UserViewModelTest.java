package com.example.chess.ui.welcome.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.MutableLiveData;

import com.example.chess.model.Result;
import com.example.chess.model.User;
import com.example.chess.repository.user.IChessUserRepository;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test (local) per UserViewModel.
 *
 * Il ViewModel viene testato in isolamento sostituendo il repository reale
 * (Firebase) con un TEST DOUBLE di tipo "fake" che implementa
 * IChessUserRepository: registra le chiamate ricevute e restituisce
 * risultati predefiniti, senza alcuna dipendenza dalla rete o da Firebase.
 */
public class UserViewModelTest {

    private static final User TEST_USER = new User("Mario", "mario@test.it", "token-123");

    private FakeUserRepository fakeRepository;
    private UserViewModel viewModel;

    /** Test double (fake) del repository utente. */
    private static class FakeUserRepository implements IChessUserRepository {

        // Risultati predefiniti che il fake restituirà
        MutableLiveData<Result> nextResult = new MutableLiveData<>();
        User loggedUser = null;

        // Registrazione delle chiamate ricevute (per verificare la delega)
        String lastName, lastEmail, lastPassword, lastIdToken, lastResetEmail;
        boolean lastIsUserRegistered;
        int logoutCallCount = 0;

        @Override
        public MutableLiveData<Result> getUser(String name, String email, String password, boolean isUserRegistered) {
            this.lastName = name;
            this.lastEmail = email;
            this.lastPassword = password;
            this.lastIsUserRegistered = isUserRegistered;
            return nextResult;
        }

        @Override
        public MutableLiveData<Result> getGoogleUser(String idToken) {
            this.lastIdToken = idToken;
            return nextResult;
        }

        @Override
        public MutableLiveData<Result> logout() {
            this.logoutCallCount++;
            return nextResult;
        }

        @Override
        public MutableLiveData<Result> resetPassword(String email) {
            this.lastResetEmail = email;
            return nextResult;
        }

        @Override
        public User getLoggedUser() {
            return loggedUser;
        }
    }

    @Before
    public void setUp() {
        fakeRepository = new FakeUserRepository();
        viewModel = new UserViewModel(fakeRepository);
    }

    @Test
    public void getUser_delegatesParametersToRepository_andReturnsItsLiveData() {
        fakeRepository.nextResult = new MutableLiveData<>(new Result.Success(TEST_USER));

        MutableLiveData<Result> result =
                viewModel.getUser("Mario", "mario@test.it", "password123", false);

        // Il ViewModel deve delegare al repository senza alterare i parametri
        assertEquals("Mario", fakeRepository.lastName);
        assertEquals("mario@test.it", fakeRepository.lastEmail);
        assertEquals("password123", fakeRepository.lastPassword);
        assertFalse(fakeRepository.lastIsUserRegistered);

        // ...e restituire la stessa LiveData prodotta dal repository
        assertSame(fakeRepository.nextResult, result);
        assertTrue(result.getValue().isSuccess());
        assertSame(TEST_USER, ((Result.Success) result.getValue()).getUser());
    }

    @Test
    public void getUser_loginFailure_propagatesErrorResult() {
        fakeRepository.nextResult = new MutableLiveData<>(new Result.Error("Credenziali errate"));

        MutableLiveData<Result> result =
                viewModel.getUser(null, "mario@test.it", "wrong", true);

        assertTrue(fakeRepository.lastIsUserRegistered);
        assertFalse(result.getValue().isSuccess());
        assertEquals("Credenziali errate", ((Result.Error) result.getValue()).getMessage());
    }

    @Test
    public void getGoogleUser_delegatesIdToken() {
        fakeRepository.nextResult = new MutableLiveData<>(new Result.Success(TEST_USER));

        MutableLiveData<Result> result = viewModel.getGoogleUser("google-token-xyz");

        assertEquals("google-token-xyz", fakeRepository.lastIdToken);
        assertTrue(result.getValue().isSuccess());
    }

    @Test
    public void resetPassword_delegatesEmail() {
        fakeRepository.nextResult = new MutableLiveData<>(new Result.Success(null));

        viewModel.resetPassword("mario@test.it");

        assertEquals("mario@test.it", fakeRepository.lastResetEmail);
    }

    @Test
    public void logout_invokesRepositoryExactlyOnce() {
        viewModel.logout();
        assertEquals(1, fakeRepository.logoutCallCount);
    }

    @Test
    public void getLoggedUser_returnsUserFromRepository() {
        fakeRepository.loggedUser = TEST_USER;
        assertSame(TEST_USER, viewModel.getLoggedUser());
    }

    @Test
    public void authenticationError_flagDefaultsToFalse_andIsSettable() {
        assertFalse(viewModel.isAuthenticationError());
        viewModel.setAuthenticationError(true);
        assertTrue(viewModel.isAuthenticationError());
    }
}
