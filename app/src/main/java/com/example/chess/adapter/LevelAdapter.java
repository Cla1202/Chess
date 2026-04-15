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

        // Imposta il testo corretto in base alla posizione (es. Livello 1, Livello 2)
        holder.titleText.setText("Livello " + levelNumber);
        holder.descriptionText.setText(level.getTitle()); // Es: "Matto in 2"

        // LOGICA DI SBLOCCO/BLOCCO
        if (levelNumber <= maxUnlocked) {
            // --- LIVELLO SBLOCCATO ---
            holder.padlockIcon.setVisibility(View.GONE); // Nascondi lucchetto

            // Colore di sfondo normale (es. grigio scuro)
            holder.itemView.setBackgroundColor(Color.parseColor("#2C2C2C"));

            // Testo verde per far capire che è giocabile
            holder.titleText.setTextColor(Color.parseColor("#769656"));

            // Permetti il click per giocare
            holder.itemView.setOnClickListener(v -> listener.onItemClick(position));

        } else {
            // --- LIVELLO BLOCCATO ---
            holder.padlockIcon.setVisibility(View.VISIBLE); // Mostra lucchetto

            // Colore di sfondo più scuro e spento per far capire che è inattivo
            holder.itemView.setBackgroundColor(Color.parseColor("#151515"));

            // Testo grigio
            holder.titleText.setTextColor(Color.parseColor("#555555"));

            // Gestisci il click avvisando l'utente
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(), "Devi superare il livello precedente per sbloccare questo!", Toast.LENGTH_SHORT).show();
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