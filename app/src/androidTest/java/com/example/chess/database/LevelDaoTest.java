package com.example.chess.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test INSTRUMENTED (medium/integration test) per LevelDao.
 * Va eseguito su dispositivo fisico o emulatore (cartella androidTest).
 *
 * Usa un database Room IN MEMORIA come test double (fake) del database reale:
 * comportamento identico, ma più veloce e senza effetti persistenti
 * (i dati spariscono alla chiusura).
 */
@RunWith(AndroidJUnit4.class)
public class LevelDaoTest {

    private ChessDatabase database;
    private LevelDao dao;

    private static LevelProgress completed(int levelId, String userId) {
        return new LevelProgress(levelId, userId, true, 0, 30_000L, System.currentTimeMillis());
    }

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, ChessDatabase.class)
                .allowMainThreadQueries() // accettabile SOLO nei test
                .build();
        dao = database.levelDao();
    }

    @After
    public void closeDb() {
        database.close();
    }

    // ==========================================================
    // INSERIMENTO E LETTURA
    // ==========================================================

    @Test
    public void insertProgress_andReadItBack() {
        LevelProgress progress = new LevelProgress(1, "user1", true, 2, 45_000L, 1234L);
        dao.insertProgress(progress);

        LevelProgress loaded = dao.getProgressForLevel(1, "user1");
        assertNotNull(loaded);
        assertEquals(1, loaded.levelId);
        assertEquals("user1", loaded.userId);
        assertTrue(loaded.isCompleted);
        assertEquals(2, loaded.mistakesMade);
        assertEquals(45_000L, loaded.timeSpentMillis);
        assertEquals(1234L, loaded.completionTimestamp);
    }

    @Test
    public void getProgressForLevel_missingRow_returnsNull() {
        assertNull(dao.getProgressForLevel(99, "nobody"));
    }

    @Test
    public void insertProgress_sameLevelAndUser_replacesExistingRow() {
        // Strategia OnConflictStrategy.REPLACE sulla chiave composta (levelId, userId)
        dao.insertProgress(new LevelProgress(1, "user1", false, 3, 10_000L, 0L));
        dao.insertProgress(new LevelProgress(1, "user1", true, 1, 20_000L, 555L));

        LevelProgress loaded = dao.getProgressForLevel(1, "user1");
        assertNotNull(loaded);
        assertTrue(loaded.isCompleted);
        assertEquals(1, loaded.mistakesMade);
        assertEquals(1, dao.getAllCompletedLevelsForUser("user1").size());
    }

    // ==========================================================
    // ISOLAMENTO TRA UTENTI
    // ==========================================================

    @Test
    public void progress_isIsolatedPerUser() {
        dao.insertProgress(completed(1, "user1"));
        dao.insertProgress(completed(2, "user1"));
        dao.insertProgress(completed(5, "user2"));

        assertEquals(2, dao.getAllCompletedLevelsForUser("user1").size());
        assertEquals(1, dao.getAllCompletedLevelsForUser("user2").size());
        assertNull(dao.getProgressForLevel(5, "user1"));
    }

    @Test
    public void getAllCompletedLevelsForUser_excludesNotCompletedLevels() {
        dao.insertProgress(completed(1, "user1"));
        dao.insertProgress(new LevelProgress(2, "user1", false, 5, 10_000L, 0L));

        List<LevelProgress> completedLevels = dao.getAllCompletedLevelsForUser("user1");
        assertEquals(1, completedLevels.size());
        assertEquals(1, completedLevels.get(0).levelId);
    }

    // ==========================================================
    // CANCELLAZIONE
    // ==========================================================

    @Test
    public void deleteProgressForUser_removesOnlyThatUser() {
        dao.insertProgress(completed(1, "user1"));
        dao.insertProgress(completed(1, "user2"));

        dao.deleteProgressForUser("user1");

        assertTrue(dao.getAllCompletedLevelsForUser("user1").isEmpty());
        assertEquals(1, dao.getAllCompletedLevelsForUser("user2").size());
    }

    // ==========================================================
    // QUERY LIVEDATA
    // ==========================================================

    @Test
    public void getMaxCompletedLevelLive_returnsHighestCompletedLevel() throws InterruptedException {
        dao.insertProgress(completed(1, "user1"));
        dao.insertProgress(completed(3, "user1"));
        dao.insertProgress(new LevelProgress(7, "user1", false, 0, 0L, 0L)); // non completato
        dao.insertProgress(completed(10, "user2")); // altro utente

        Integer max = getOrAwaitValue(dao.getMaxCompletedLevelLive("user1"));
        assertEquals(Integer.valueOf(3), max);
    }

    @Test
    public void getMaxCompletedLevelLive_noData_returnsNull() throws InterruptedException {
        Integer max = getOrAwaitValue(dao.getMaxCompletedLevelLive("ghost"));
        assertNull(max);
    }

    @Test
    public void getAllCompletedLevelsLive_isOrderedByCompletionTimestamp() throws InterruptedException {
        dao.insertProgress(new LevelProgress(2, "user1", true, 0, 0L, 2000L));
        dao.insertProgress(new LevelProgress(1, "user1", true, 0, 0L, 3000L));
        dao.insertProgress(new LevelProgress(3, "user1", true, 0, 0L, 1000L));

        List<LevelProgress> levels = getOrAwaitValue(dao.getAllCompletedLevelsLive("user1"));
        assertEquals(3, levels.size());
        assertEquals(3, levels.get(0).levelId); // timestamp 1000
        assertEquals(2, levels.get(1).levelId); // timestamp 2000
        assertEquals(1, levels.get(2).levelId); // timestamp 3000
    }

    // ==========================================================
    // UTILITY: lettura sincrona di una LiveData nei test
    // ==========================================================

    @SuppressWarnings("unchecked")
    private static <T> T getOrAwaitValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        final Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T value) {
                data[0] = value;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        // observeForever deve essere chiamata sul main thread
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(() -> liveData.observeForever(observer));

        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("La LiveData non ha mai emesso un valore");
        }
        return (T) data[0];
    }
}
