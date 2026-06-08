package com.example.chess.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

    // Add this method to allow the ViewModel to update levels in real time
    public void updateMaxUnlocked(int newMaxUnlocked) {
        this.maxUnlocked = newMaxUnlocked;
        notifyDataSetChanged(); // Redraw the entire list
    }

    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ENSURE that the name of your XML layout below is correct (e.g. item_level_card)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_card, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        QuizLevel level = levels.get(position);

        // Calculate the level number (position starts from 0, so we add 1)
        int levelNumber = position + 1;

        // USE THE CONSTANT FOR THE TEXT PREFIX
        holder.titleText.setText(Constants.PREFIX_LEVEL + levelNumber);
        holder.descriptionText.setText(level.getTitle()); // e.g. "Mate in 2"

        // UNLOCK/LOCK LOGIC
        if (levelNumber <= maxUnlocked) {
            // --- LEVEL UNLOCKED ---
            holder.padlockIcon.setVisibility(View.GONE);

            // USE CONSTANTS FOR UNLOCKED COLORS
            holder.itemView.setBackgroundColor(Color.parseColor(Constants.COLOR_CARD_UNLOCKED));

            // Here I call COLOR_DARK which you already had in your original Constants
            holder.titleText.setTextColor(Color.parseColor(Constants.COLOR_DARK));

            // Allow clicking to play
            holder.itemView.setOnClickListener(v -> listener.onItemClick(position));

        } else {
            // --- LEVEL LOCKED ---
            holder.padlockIcon.setVisibility(View.VISIBLE);

            // USE CONSTANTS FOR LOCKED COLORS
            holder.itemView.setBackgroundColor(Color.parseColor(Constants.COLOR_CARD_LOCKED));
            holder.titleText.setTextColor(Color.parseColor(Constants.COLOR_TEXT_LOCKED));

            // USE THE CONSTANT FOR THE TOAST MESSAGE
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(), Constants.MSG_LEVEL_LOCKED, Toast.LENGTH_SHORT).show();
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
            // ENSURE THESE IDs MATCH THOSE IN YOUR item_level_card.xml
            // For example, in your XML the title might be called levelTitleTextView
            titleText = itemView.findViewById(R.id.levelTitleText);
            descriptionText = itemView.findViewById(R.id.levelNumberText);
            padlockIcon = itemView.findViewById(R.id.lockIcon);
        }


    }



}