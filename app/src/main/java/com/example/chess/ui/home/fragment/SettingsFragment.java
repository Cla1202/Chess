package com.example.chess.ui.home.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.chess.R;
import com.example.chess.database.ChessDatabase;
import com.example.chess.ui.home.HomeActivity;
import com.example.chess.util.ChessUtil;
import com.example.chess.util.Constants;

import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        prefs = requireContext().getSharedPreferences(Constants.SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);

        TextView tvBoard = v.findViewById(R.id.tv_board_theme_value);
        TextView tvPiece = v.findViewById(R.id.tv_piece_style_value);
        TextView tvLang = v.findViewById(R.id.tv_language_value);
        SwitchCompat swVibr = v.findViewById(R.id.switch_vibration);
        SwitchCompat swTimer = v.findViewById(R.id.switch_timer);

        // Load internal keys and display translated names
        String boardKey = prefs.getString(Constants.KEY_BOARD_THEME, Constants.THEME_GREEN);
        tvBoard.setText(getLocalizedBoardTheme(boardKey));

        String pieceKey = prefs.getString(Constants.KEY_PIECE_STYLE, Constants.STYLE_NEO);
        tvPiece.setText(getLocalizedPieceStyle(pieceKey));

        swVibr.setChecked(prefs.getBoolean(Constants.KEY_VIBRATION, true));
        swTimer.setChecked(prefs.getBoolean(Constants.KEY_TIMER_ENABLED, true));

        String lang = prefs.getString(Constants.KEY_LANGUAGE, "it");
        tvLang.setText(lang.equals("en") ? R.string.lingua_inglese : (lang.equals("es") ? R.string.lingua_spagnolo : R.string.lingua_italiano));

        v.findViewById(R.id.layout_board_theme).setOnClickListener(view -> {
            String[] names = {
                    getString(R.string.verde_classico),
                    getString(R.string.legno_scuro),
                    getString(R.string.blu_oceano),
                    getString(R.string.grigio_moderno)
            };
            String[] keys = {Constants.THEME_GREEN, Constants.THEME_WOOD, Constants.THEME_OCEAN, Constants.THEME_GREY};
            new AlertDialog.Builder(requireContext()).setTitle(R.string.tema_scacchiera).setItems(names, (d, w) -> {
                tvBoard.setText(names[w]); 
                prefs.edit().putString(Constants.KEY_BOARD_THEME, keys[w]).apply();
            }).show();
        });

        v.findViewById(R.id.layout_piece_style).setOnClickListener(view -> {
            String[] names = {
                    getString(R.string.neo),
                    getString(R.string.classico),
                    getString(R.string.moderno),
                    getString(R.string.alfa)
            };
            String[] keys = {Constants.STYLE_NEO, Constants.STYLE_CLASSIC, Constants.STYLE_MODERN, Constants.STYLE_ALPHA};
            new AlertDialog.Builder(requireContext()).setTitle(R.string.stile_pezzi).setItems(names, (d, w) -> {
                tvPiece.setText(names[w]); 
                prefs.edit().putString(Constants.KEY_PIECE_STYLE, keys[w]).apply();
            }).show();
        });

        v.findViewById(R.id.layout_language).setOnClickListener(view -> {
            String[] l = {getString(R.string.lingua_italiano), getString(R.string.lingua_inglese), getString(R.string.lingua_spagnolo)};
            String[] c = {"it", "en", "es"};
            new AlertDialog.Builder(requireContext()).setTitle(R.string.lingua).setItems(l, (d, w) -> {
                prefs.edit().putString(Constants.KEY_LANGUAGE, c[w]).apply();
                ChessUtil.applyLocale(requireContext());
                requireActivity().recreate();
            }).show();
        });

        swVibr.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean(Constants.KEY_VIBRATION, checked).apply());
        swTimer.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean(Constants.KEY_TIMER_ENABLED, checked).apply());

        v.findViewById(R.id.btn_reset_progress).setOnClickListener(view -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.reset_titolo)
                    .setMessage(R.string.reset_messaggio)
                    .setPositiveButton(R.string.si, (d, w) -> {
                        if (getActivity() instanceof HomeActivity) {
                            HomeActivity activity = (HomeActivity) getActivity();
                            if (activity.getCurrentUser() != null) {
                                String userId = activity.getCurrentUser().getIdToken();

                                Executors.newSingleThreadExecutor().execute(() -> {
                                    ChessDatabase.getInstance(requireContext()).levelDao().deleteProgressForUser(userId);

                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(), R.string.progressi_azzerati, Toast.LENGTH_SHORT).show()
                                    );
                                });
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        v.findViewById(R.id.btn_exit_app).setOnClickListener(view -> {
            requireActivity().finishAffinity(); System.exit(0);
        });

        return v;
    }

    private String getLocalizedBoardTheme(String key) {
        if (key == null) return getString(R.string.verde_classico);
        if (Constants.THEME_WOOD.equals(key)) return getString(R.string.legno_scuro);
        if (Constants.THEME_OCEAN.equals(key)) return getString(R.string.blu_oceano);
        if (Constants.THEME_GREY.equals(key)) return getString(R.string.grigio_moderno);
        return getString(R.string.verde_classico);
    }

    private String getLocalizedPieceStyle(String key) {
        if (key == null) return getString(R.string.neo);
        if (Constants.STYLE_CLASSIC.equals(key)) return getString(R.string.classico);
        if (Constants.STYLE_MODERN.equals(key)) return getString(R.string.moderno);
        if (Constants.STYLE_ALPHA.equals(key)) return getString(R.string.alfa);
        return getString(R.string.neo);
    }
}
