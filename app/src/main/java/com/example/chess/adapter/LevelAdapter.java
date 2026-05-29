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

    // Aggiungi questo metodo per permettere al ViewModel di aggiornare i livelli in tempo reale
    public void updateMaxUnlocked(int newMaxUnlocked) {
        this.maxUnlocked = newMaxUnlocked;
        notifyDataSetChanged(); // Ridisegna tutta la lista
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ASSICURATI che il nome del tuo layout XML qui sotto sia corretto (es. item_level_card)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_card, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        QuizLevel level = levels.get(position);

        // Calcoliamo il numero del livello (la position parte da 0, quindi aggiungiamo 1)
        int levelNumber = position + 1;

        // USA LA COSTANTE PER IL PREFISSO TESTUALE
        holder.titleText.setText(Constants.PREFIX_LEVEL + levelNumber);
        holder.descriptionText.setText(level.getTitle()); // Es: "Matto in 2"

        // LOGICA DI SBLOCCO/BLOCCO
        if (levelNumber <= maxUnlocked) {
            // --- LIVELLO SBLOCCATO ---
            holder.padlockIcon.setVisibility(View.GONE);

            // USA LE COSTANTI PER I COLORI SBLOCCATI
            holder.itemView.setBackgroundColor(Color.parseColor(Constants.COLOR_CARD_UNLOCKED));

            // Qui richiamo COLOR_DARK che avevi già nel tuo Constants originale
            holder.titleText.setTextColor(Color.parseColor(Constants.COLOR_DARK));

            // Permetti il click per giocare
            holder.itemView.setOnClickListener(v -> listener.onItemClick(position));

        } else {
            // --- LIVELLO BLOCCATO ---
            holder.padlockIcon.setVisibility(View.VISIBLE);

            // USA LE COSTANTI PER I COLORI BLOCCATI
            holder.itemView.setBackgroundColor(Color.parseColor(Constants.COLOR_CARD_LOCKED));
            holder.titleText.setTextColor(Color.parseColor(Constants.COLOR_TEXT_LOCKED));

            // USA LA COSTANTE PER IL MESSAGGIO TOAST
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
            // ASSICURATI CHE QUESTI ID CORRISPONDANO A QUELLI DEL TUO item_level_card.xml
            // Ad esempio, nel tuo XML il titolo potrebbe chiamarsi levelTitleTextView
            titleText = itemView.findViewById(R.id.levelTitleText);
            descriptionText = itemView.findViewById(R.id.levelNumberText);
            padlockIcon = itemView.findViewById(R.id.lockIcon);
        }


    }



}