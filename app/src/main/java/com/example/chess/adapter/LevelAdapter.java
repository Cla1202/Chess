package com.example.chess.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chess.R;
import com.example.chess.model.QuizLevel;
import com.example.chess.util.Constants;

import java.util.List;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    private List<QuizLevel> levels;
    private int maxUnlocked;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public LevelAdapter(List<QuizLevel> levels, int maxUnlocked, OnItemClickListener listener) {
        this.levels = levels;
        this.maxUnlocked = maxUnlocked;
        this.listener = listener;
    }

    public void updateMaxUnlocked(int newMaxUnlocked) {
        this.maxUnlocked = newMaxUnlocked;
        notifyDataSetChanged();
    }

    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_card, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        QuizLevel level = levels.get(position);
        int levelNumber = position + 1;
        android.content.Context context = holder.itemView.getContext();

        String prefix = context.getString(R.string.prefix_level);
        holder.titleText.setText(prefix + levelNumber);
        holder.descriptionText.setText(level.getTitle());

        if (levelNumber <= maxUnlocked) {
            holder.padlockIcon.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_card_unlocked));
            holder.titleText.setTextColor(Color.parseColor(Constants.THEME_CLASSIC_DARK));
            holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
        } else {
            holder.padlockIcon.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_card_locked));
            holder.titleText.setTextColor(ContextCompat.getColor(context, R.color.color_text_locked));
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(context, context.getString(R.string.msg_level_locked), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    public static class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;
        ImageView padlockIcon;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.levelTitleText);
            descriptionText = itemView.findViewById(R.id.levelNumberText);
            padlockIcon = itemView.findViewById(R.id.lockIcon);
        }
    }
}