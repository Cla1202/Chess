package com.example.chess.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.example.chess.R;
import com.example.chess.database.ChessDatabase;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "ChessSettings";
    private static final String KEY_BOARD_THEME = "board_theme";
    private static final String KEY_PIECE_STYLE = "piece_style";
    private static final String KEY_SOUNDS = "sounds_enabled";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_CONFIRM_MOVE = "confirm_move_enabled";
    private static final String KEY_VIBRATION = "vibration_enabled";
    private static final String KEY_LANGUAGE = "language";

    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Riferimenti ai componenti
        View layoutBoardTheme = view.findViewById(R.id.layout_board_theme);
        TextView tvBoardThemeValue = view.findViewById(R.id.tv_board_theme_value);
        View layoutPieceStyle = view.findViewById(R.id.layout_piece_style);
        TextView tvPieceStyleValue = view.findViewById(R.id.tv_piece_style_value);
        View layoutLanguage = view.findViewById(R.id.layout_language);
        TextView tvLanguageValue = view.findViewById(R.id.tv_language_value);
        
        SwitchCompat switchSounds = view.findViewById(R.id.switch_sounds);
        SwitchCompat switchNotifications = view.findViewById(R.id.switch_notifications);
        SwitchCompat switchConfirmMove = view.findViewById(R.id.switch_confirm_move);
        SwitchCompat switchVibration = view.findViewById(R.id.switch_vibration);
        
        Button btnResetProgress = view.findViewById(R.id.btn_reset_progress);
        Button btnInfo = view.findViewById(R.id.btn_info);
        Button btnExitApp = view.findViewById(R.id.btn_exit_app);

        // Caricamento stati salvati
        loadSettings(tvBoardThemeValue, tvPieceStyleValue, tvLanguageValue, switchSounds, switchNotifications, switchConfirmMove, switchVibration);

        // Listener
        layoutBoardTheme.setOnClickListener(v -> {
            String[] themes = {"Verde Classico", "Legno Scuro", "Blu Oceano", "Grigio Moderno"};
            showSelectionDialog("Scegli Tema Scacchiera", themes, tvBoardThemeValue, KEY_BOARD_THEME);
        });

        layoutPieceStyle.setOnClickListener(v -> {
            String[] styles = {"Neo", "Classico", "Moderno", "Alfa"};
            showSelectionDialog("Scegli Stile Pezzi", styles, tvPieceStyleValue, KEY_PIECE_STYLE);
        });

        layoutLanguage.setOnClickListener(v -> {
            String[] languages = {getString(R.string.lingua_italiano), getString(R.string.lingua_inglese), getString(R.string.lingua_spagnolo)};
            String[] codes = {"it", "en", "es"};
            
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.lingua)
                    .setItems(languages, (dialog, which) -> {
                        String selectedLang = languages[which];
                        String selectedCode = codes[which];
                        tvLanguageValue.setText(selectedLang);
                        saveString(KEY_LANGUAGE, selectedCode);
                        setLocale(selectedCode);
                    })
                    .show();
        });

        switchSounds.setOnCheckedChangeListener((b, isChecked) -> saveBoolean(KEY_SOUNDS, isChecked));
        switchNotifications.setOnCheckedChangeListener((b, isChecked) -> saveBoolean(KEY_NOTIFICATIONS, isChecked));
        switchConfirmMove.setOnCheckedChangeListener((b, isChecked) -> saveBoolean(KEY_CONFIRM_MOVE, isChecked));
        switchVibration.setOnCheckedChangeListener((b, isChecked) -> saveBoolean(KEY_VIBRATION, isChecked));

        btnResetProgress.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reset Progressi")
                    .setMessage("Sei sicuro di voler cancellare tutti i livelli superati? L'azione è irreversibile.")
                    .setPositiveButton("Sì, resetta", (dialog, which) -> resetUserProgress())
                    .setNegativeButton("Annulla", null)
                    .show();
        });

        btnInfo.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Informazioni App")
                    .setMessage("Unimib Chess v1.0\nSviluppata per il progetto di Programmazione Mobile.")
                    .setPositiveButton("Chiudi", null)
                    .show();
        });

        btnExitApp.setOnClickListener(v -> {
            requireActivity().finishAffinity();
            System.exit(0);
        });

        return view;
    }

    private void loadSettings(TextView tvBoard, TextView tvPiece, TextView tvLang, SwitchCompat swSounds, SwitchCompat swNotif, SwitchCompat swConfirm, SwitchCompat swVibr) {
        tvBoard.setText(sharedPreferences.getString(KEY_BOARD_THEME, "Verde Classico"));
        tvPiece.setText(sharedPreferences.getString(KEY_PIECE_STYLE, "Classico"));
        
        String langCode = sharedPreferences.getString(KEY_LANGUAGE, "it");
        switch (langCode) {
            case "en": tvLang.setText(R.string.lingua_inglese); break;
            case "es": tvLang.setText(R.string.lingua_spagnolo); break;
            default: tvLang.setText(R.string.lingua_italiano); break;
        }

        swSounds.setChecked(sharedPreferences.getBoolean(KEY_SOUNDS, true));
        swNotif.setChecked(sharedPreferences.getBoolean(KEY_NOTIFICATIONS, false));
        swConfirm.setChecked(sharedPreferences.getBoolean(KEY_CONFIRM_MOVE, false));
        swVibr.setChecked(sharedPreferences.getBoolean(KEY_VIBRATION, true));
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        config.setLocale(locale);
        resources.updateConfiguration(config, dm);
        
        // Riavvia l'activity per applicare la lingua
        requireActivity().recreate();
    }

    private void resetUserProgress() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ChessDatabase db = ChessDatabase.getInstance(requireContext());
            db.levelDao().deleteAllProgress();
            requireActivity().runOnUiThread(() -> 
                Toast.makeText(getContext(), "Progressi resettati con successo", Toast.LENGTH_SHORT).show());
        });
    }

    private void showSelectionDialog(String title, String[] items, TextView targetView, String key) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(items, (dialog, which) -> {
                    String selected = items[which];
                    targetView.setText(selected);
                    saveString(key, selected);
                })
                .show();
    }

    private void saveString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    private void saveBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }
}