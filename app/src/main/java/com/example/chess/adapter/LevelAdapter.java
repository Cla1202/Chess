package com.example.chess.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chess.R;
import com.example.chess.model.QuizLevel;
import java.util.List;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    private List<QuizLevel> levelList;
    private OnLevelClickListener listener;
    private int maxUnlockedLevel;
    // Interfaccia per gestire il click sulle carte
    public interface OnLevelClickListener {
        void onLevelClick(int position);
    }

    // 2. ASSICURATI CHE IL COSTRUTTORE ABBIA 3 PARAMETRI:
    public LevelAdapter(List<QuizLevel> levelList, int maxUnlockedLevel, OnLevelClickListener listener) {
        this.levelList = levelList;
        this.maxUnlockedLevel = maxUnlockedLevel; // E che venga salvato qui
        this.listener = listener;
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_card, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        QuizLevel level = levelList.get(position);

        holder.levelNumberText.setText("Livello " + level.getLevelId());
        holder.levelTitleText.setText(level.getTitle());

        // Gestione del click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLevelClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return levelList.size();
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView levelNumberText, levelTitleText;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            levelNumberText = itemView.findViewById(R.id.levelNumberText);
            levelTitleText = itemView.findViewById(R.id.levelTitleText);
        }
    }
}