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

import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        prefs = requireContext().getSharedPreferences("ChessSettings", Context.MODE_PRIVATE);

        TextView tvBoard = v.findViewById(R.id.tv_board_theme_value);
        TextView tvPiece = v.findViewById(R.id.tv_piece_style_value);
        TextView tvLang = v.findViewById(R.id.tv_language_value);
        SwitchCompat swVibr = v.findViewById(R.id.switch_vibration);
        SwitchCompat swTimer = v.findViewById(R.id.switch_timer);

        tvBoard.setText(prefs.getString("board_theme", "Verde Classico"));
        tvPiece.setText(prefs.getString("piece_style", "Neo"));
        swVibr.setChecked(prefs.getBoolean("vibration_enabled", true));
        swTimer.setChecked(prefs.getBoolean("timer_enabled", true));

        String lang = prefs.getString("language", "it");
        tvLang.setText(lang.equals("en") ? R.string.lingua_inglese : (lang.equals("es") ? R.string.lingua_spagnolo : R.string.lingua_italiano));

        v.findViewById(R.id.layout_board_theme).setOnClickListener(view -> {
            String[] t = {"Verde Classico", "Legno Scuro", "Blu Oceano", "Grigio Moderno"};
            new AlertDialog.Builder(requireContext()).setTitle(R.string.tema_scacchiera).setItems(t, (d, w) -> {
                tvBoard.setText(t[w]); prefs.edit().putString("board_theme", t[w]).apply();
            }).show();
        });

        v.findViewById(R.id.layout_piece_style).setOnClickListener(view -> {
            String[] s = {"Neo", "Classico", "Moderno", "Alfa"};
            new AlertDialog.Builder(requireContext()).setTitle(R.string.stile_pezzi).setItems(s, (d, w) -> {
                tvPiece.setText(s[w]); prefs.edit().putString("piece_style", s[w]).apply();
            }).show();
        });

        v.findViewById(R.id.layout_language).setOnClickListener(view -> {
            String[] l = {getString(R.string.lingua_italiano), getString(R.string.lingua_inglese), getString(R.string.lingua_spagnolo)};
            String[] c = {"it", "en", "es"};
            new AlertDialog.Builder(requireContext()).setTitle(R.string.lingua).setItems(l, (d, w) -> {
                prefs.edit().putString("language", c[w]).apply();
                ChessUtil.applyLocale(requireContext());
                requireActivity().recreate();
            }).show();
        });

        swVibr.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean("vibration_enabled", checked).apply());
        swTimer.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean("timer_enabled", checked).apply());

        // Modification: Reset progress ONLY for the logged-in user
        v.findViewById(R.id.btn_reset_progress).setOnClickListener(view -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reset")
                    .setMessage("Sei sicuro di voler cancellare i tuoi progressi nei Quiz?")
                    .setPositiveButton("Sì", (d, w) -> {
                        if (getActivity() instanceof HomeActivity) {
                            HomeActivity activity = (HomeActivity) getActivity();
                            if (activity.getCurrentUser() != null) {
                                String userId = activity.getCurrentUser().getIdToken();

                                Executors.newSingleThreadExecutor().execute(() -> {
                                    // Use the new DAO method that only deletes data for userId
                                    ChessDatabase.getInstance(requireContext()).levelDao().deleteProgressForUser(userId);

                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(), "Progressi azzerati!", Toast.LENGTH_SHORT).show()
                                    );
                                });
                            }
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        v.findViewById(R.id.btn_exit_app).setOnClickListener(view -> {
            requireActivity().finishAffinity(); System.exit(0);
        });

        return v;
    }
}