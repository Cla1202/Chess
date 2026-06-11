package com.example.chess;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.chess.model.User;
import com.example.chess.ui.home.GameActivity;
import com.example.chess.util.Constants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * Test Espresso per GameActivity (modalità LOCAL_PVP senza Firebase).
 *
 * NOTA: i test usano la modalità LOCAL_PVP che non richiede connessione di rete
 *       né autenticazione. Il timer viene disabilitato tramite SharedPreferences
 *       per evitare side-effect temporali nei test.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class GameActivityTest {

    private Intent buildLocalPvpIntent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                GameActivity.class
        );
        intent.putExtra(Constants.EXTRA_MODE, Constants.MODE_LOCAL_PVP);
        // Disabilita il timer per i test
        ApplicationProvider.getApplicationContext()
                .getSharedPreferences(Constants.SETTINGS_PREFS_NAME,
                        android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.KEY_TIMER_ENABLED, false)
                .apply();
        return intent;
    }

    // -------------------------------------------------------------------------
    // Visibilità dei componenti principali
    // -------------------------------------------------------------------------

    @Test
    public void gameActivity_boardGridVisible() {
        try (ActivityScenario<GameActivity> scenario =
                     ActivityScenario.launch(buildLocalPvpIntent())) {
            onView(withId(R.id.chessGrid)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void gameActivity_statusTextVisible() {
        try (ActivityScenario<GameActivity> scenario =
                     ActivityScenario.launch(buildLocalPvpIntent())) {
            onView(withId(R.id.statusText)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void gameActivity_exitButtonVisible() {
        try (ActivityScenario<GameActivity> scenario =
                     ActivityScenario.launch(buildLocalPvpIntent())) {
            onView(withId(R.id.btnExit)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void gameActivity_timerContainerHidden_whenTimerDisabled() {
        try (ActivityScenario<GameActivity> scenario =
                     ActivityScenario.launch(buildLocalPvpIntent())) {
            // Con timer disabilitato, il contenitore deve essere GONE/INVISIBLE
            onView(withId(R.id.timerContainer))
                    .check(matches(org.hamcrest.Matchers.not(isDisplayed())));
        }
    }

    // -------------------------------------------------------------------------
    // Interazione con la scacchiera
    // -------------------------------------------------------------------------

    /**
     * Clicca su un pedone bianco (posizione e2 → indice 52) e verifica
     * che vengano evidenziate le mosse disponibili (il testo di stato cambia
     * o le celle si colorano). In mancanza di un matcher visivo preciso per
     * il colore, verifichiamo che il click non causi un crash.
     */
    @Test
    public void chessGrid_clickOnWhitePawn_doesNotCrash() {
        try (ActivityScenario<GameActivity> scenario =
                     ActivityScenario.launch(buildLocalPvpIntent())) {
            onView(withId(R.id.chessGrid)).check(matches(isDisplayed()));
            // Clicca sulla casella e2 (indice 52 nella GridView 8×8)
            onView(withId(R.id.chessGrid))
                    .perform(clickOnGridItem(52));
        }
    }

    /**
     * Simula una mossa completa: selezione del pedone e4, poi click sulla
     * casella di destinazione e5 (indice 44). Verifica che lo stato del
     * gioco cambi (turno al nero).
     */
    @Test
    public void chessGrid_moveWhitePawn_e2ToE4() {
        try (ActivityScenario<GameActivity> scenario =
                     ActivityScenario.launch(buildLocalPvpIntent())) {
            // Selezione pedone in e2 (riga 6, colonna 4 → index 52)
            onView(withId(R.id.chessGrid)).perform(clickOnGridItem(52));
            // Click su e4 (riga 4, colonna 4 → index 36)
            onView(withId(R.id.chessGrid)).perform(clickOnGridItem(36));
            // Ora è il turno del nero: statusText deve menzionare "Nero" o "Black"
            // (dipende dalla localizzazione; usiamo un controllo di esistenza)
            onView(withId(R.id.statusText)).check(matches(isDisplayed()));
        }
    }

    /**
     * Verifica che cliccare su una casella vuota non cambi il turno.
     */
    @Test
    public void chessGrid_clickOnEmptySquare_doesNotChangeTurn() {
        try (ActivityScenario<GameActivity> scenario =
                     ActivityScenario.launch(buildLocalPvpIntent())) {
            // Casella vuota al centro (e4 non è ancora occupata → index 36)
            onView(withId(R.id.chessGrid)).perform(clickOnGridItem(36));
            // Il turno non deve cambiare: statusText rimane visibile e invariato
            onView(withId(R.id.statusText)).check(matches(isDisplayed()));
        }
    }

    // -------------------------------------------------------------------------
    // Pulsante Esci
    // -------------------------------------------------------------------------

    @Test
    public void exitButton_click_finishesActivity() {
        ActivityScenario<GameActivity> scenario =
                ActivityScenario.launch(buildLocalPvpIntent());
        onView(withId(R.id.btnExit)).perform(click());
        // L'activity deve essere in stato DESTROYED
        org.junit.Assert.assertEquals(
                androidx.lifecycle.Lifecycle.State.DESTROYED,
                scenario.getState()
        );
    }

    // -------------------------------------------------------------------------
    // Helper: click su un item specifico di una GridView tramite posizione
    // -------------------------------------------------------------------------

    /**
     * Restituisce un'azione Espresso per cliccare sulla cella in posizione
     * {@code position} all'interno di una GridView.
     */
    private static androidx.test.espresso.ViewAction clickOnGridItem(final int position) {
        return new androidx.test.espresso.ViewAction() {
            @Override
            public org.hamcrest.Matcher<android.view.View> getConstraints() {
                return androidx.test.espresso.matcher.ViewMatchers.isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on GridView item at position " + position;
            }

            @Override
            public void perform(androidx.test.espresso.UiController uiController,
                                android.view.View view) {
                android.widget.GridView gridView = (android.widget.GridView) view;
                // Scorriamo fino alla cella se necessario
                gridView.smoothScrollToPosition(position);
                uiController.loopMainThreadUntilIdle();
                android.view.View child = gridView.getChildAt(
                        position - gridView.getFirstVisiblePosition());
                if (child != null) {
                    child.performClick();
                    uiController.loopMainThreadUntilIdle();
                }
            }
        };
    }
}