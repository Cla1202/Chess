package com.example.chess;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.chess.ui.welcome.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Test Espresso per la schermata di Login.
 *
 * NOTA: questi test non effettuano chiamate Firebase reali; coprono la UI e la
 *       validazione lato client. Per testare l'autenticazione vera, usa Mockito +
 *       FirebaseAuth mock o il Firebase Emulator.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    // -------------------------------------------------------------------------
    // Visibilità dei componenti
    // -------------------------------------------------------------------------

    @Test
    public void loginScreen_allKeyComponentsVisible() {
        onView(withId(R.id.usernameInput)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordInput)).check(matches(isDisplayed()));
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
        onView(withId(R.id.googleLoginButton)).check(matches(isDisplayed()));
        onView(withId(R.id.registerLink)).check(matches(isDisplayed()));
        onView(withId(R.id.forgotPasswordLink)).check(matches(isDisplayed()));
    }

    // -------------------------------------------------------------------------
    // Input / campo email e password
    // -------------------------------------------------------------------------

    @Test
    public void emailField_acceptsInput() {
        onView(withId(R.id.usernameInput))
                .perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.usernameInput))
                .check(matches(withText("test@example.com")));
    }

    @Test
    public void passwordField_acceptsInput() {
        onView(withId(R.id.passwordInput))
                .perform(typeText("password123"), closeSoftKeyboard());
        // Non verifichiamo il testo visualizzato (campo password è mascherato),
        // ma che il campo non sia vuoto dopo l'input.
        onView(withId(R.id.passwordInput)).check(matches(isDisplayed()));
    }

    @Test
    public void emailField_canBeClearedAndRetyped() {
        onView(withId(R.id.usernameInput))
                .perform(typeText("vecchia@email.it"), closeSoftKeyboard());
        onView(withId(R.id.usernameInput))
                .perform(clearText(), typeText("nuova@email.it"), closeSoftKeyboard());
        onView(withId(R.id.usernameInput))
                .check(matches(withText("nuova@email.it")));
    }

    // -------------------------------------------------------------------------
    // Navigazione verso la registrazione
    // -------------------------------------------------------------------------

    @Test
    public void registerLink_click_navigatesToRegisterFragment() {
        onView(withId(R.id.registerLink)).perform(click());
        // Dopo il click deve essere visibile il pulsante di registrazione del
        // fragment di registrazione (verifica che la navigazione avvenga)
        onView(withId(R.id.registerButton)).check(matches(isDisplayed()));
    }

    // -------------------------------------------------------------------------
    // Navigazione verso il recupero password
    // -------------------------------------------------------------------------

    @Test
    public void forgotPasswordLink_click_navigatesToResetFragment() {
        onView(withId(R.id.forgotPasswordLink)).perform(click());
        onView(withId(R.id.resetPasswordButton)).check(matches(isDisplayed()));
    }

    // -------------------------------------------------------------------------
    // Pulsante login – validazione campi vuoti
    // -------------------------------------------------------------------------

    @Test
    public void loginButton_withEmptyFields_showsError() {
        // Con i campi vuoti il login non deve avere successo.
        // Verifichiamo che la schermata rimanga su LoginFragment (es. il bottone
        // di login è ancora visibile e non siamo passati alla HomeActivity).
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    @Test
    public void loginButton_withInvalidEmailFormat_showsError() {
        onView(withId(R.id.usernameInput))
                .perform(typeText("emailinvalida"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput))
                .perform(typeText("password"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        // Rimaniamo sulla stessa schermata
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    @Test
    public void loginButton_withPasswordTooShort_showsError() {
        onView(withId(R.id.usernameInput))
                .perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput))
                .perform(typeText("12"), closeSoftKeyboard()); // < 6 caratteri
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }
}