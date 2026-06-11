package com.example.chess.repository;

import android.content.Context;
import com.example.chess.R;
import com.example.chess.model.Bishop;
import com.example.chess.model.King;
import com.example.chess.model.Knight;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Pawn;
import com.example.chess.model.Piece;
import com.example.chess.model.Queen;
import com.example.chess.model.QuizLevel;
import com.example.chess.model.Rook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Carica puzzle dal database CSV di Lichess (https://database.lichess.org/#puzzles)
 * e li converte in QuizLevel.
 */
public class LichessPuzzleLoader {
    private final Map<String, Integer> titleCounts = new HashMap<>();

    /**
     * Legge un CSV (es. da assets) e restituisce i livelli filtrati.
     */
    public List<QuizLevel> loadFromCsv(Context context,
                                       InputStream input,
                                       String themeFilter,
                                       int minRating,
                                       int maxRating,
                                       int maxLevels) throws IOException {
        List<QuizLevel> levels = new ArrayList<>();
        int levelNumber = 1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // salta l'header
            titleCounts.clear();
            while ((line = reader.readLine()) != null && levels.size() < maxLevels) {
                String[] fields = line.split(",", -1);
                if (fields.length < 8) continue;

                String fen = fields[1];
                String movesUci = fields[2];
                int rating;
                try {
                    rating = Integer.parseInt(fields[3]);
                } catch (NumberFormatException e) {
                    continue;
                }
                String themes = fields[7];

                if (rating < minRating || rating > maxRating) continue;
                if (themeFilter != null && !themes.contains(themeFilter)) continue;

                QuizLevel level = buildLevel(context, levelNumber, fen, movesUci, themes);
                if (level != null) {
                    levels.add(level);
                    levelNumber++;
                }
            }
        }
        return levels;
    }


    /**
     * Costruisce un QuizLevel da FEN + mosse UCI.
     */
    private QuizLevel buildLevel(Context context, int number, String fen, String movesUci, String themes) {
        Piece[][] board = parseFen(fen);
        if (board == null) return null;

        String[] uciMoves = movesUci.trim().split("\\s+");
        if (uciMoves.length < 2) return null;

        for (String m : uciMoves) {
            if (m.length() != 4) return null;
        }

        if (!applySimpleMove(board, uciMoves[0])) return null;

        boolean whiteToMoveInFen = fen.split(" ")[1].equals("w");
        boolean playerIsWhite = !whiteToMoveInFen;

        List<MoveRequest> soluzione = new ArrayList<>();
        for (int i = 1; i < uciMoves.length; i++) {
            soluzione.add(uciToMove(uciMoves[i]));
        }

        String baseTitle = describeTheme(context, number, themes);
        int count = titleCounts.getOrDefault(baseTitle, 0) + 1;
        titleCounts.put(baseTitle, count);
        
        // Titolo pulito (es. "Matto Arabo" o "Matto Arabo #2")
        String displayTitle = baseTitle + (count > 1 ? " #" + count : "");
        
        // Il titolo salvato nel QuizLevel è solo il nome del tema.
        // Il "Livello X" verrà aggiunto dall'Adapter.
        return new QuizLevel(number, displayTitle, board, playerIsWhite, soluzione, 3);
    }

    public Piece[][] parseFen(String fen) {
        Piece[][] board = new Piece[8][8];
        String placement = fen.split(" ")[0];
        String[] ranks = placement.split("/");
        if (ranks.length != 8) return null;

        for (int r = 0; r < 8; r++) {
            int c = 0;
            for (char ch : ranks[r].toCharArray()) {
                if (Character.isDigit(ch)) {
                    c += ch - '0';
                } else {
                    if (c > 7) return null;
                    boolean isWhite = Character.isUpperCase(ch);
                    Piece piece = createPiece(Character.toLowerCase(ch), r, c, isWhite);
                    if (piece == null) return null;
                    board[r][c] = piece;
                    c++;
                }
            }
            if (c != 8) return null;
        }
        return board;
    }

    private Piece createPiece(char type, int row, int col, boolean isWhite) {
        switch (type) {
            case 'k': return new King(row, col, isWhite);
            case 'q': return new Queen(row, col, isWhite);
            case 'r': return new Rook(row, col, isWhite);
            case 'b': return new Bishop(row, col, isWhite);
            case 'n': return new Knight(row, col, isWhite);
            case 'p': return new Pawn(row, col, isWhite);
            default:  return null;
        }
    }

    public MoveRequest uciToMove(String uci) {
        int startCol = uci.charAt(0) - 'a';
        int startRow = 8 - Character.getNumericValue(uci.charAt(1));
        int endCol = uci.charAt(2) - 'a';
        int endRow = 8 - Character.getNumericValue(uci.charAt(3));
        return new MoveRequest(startRow, startCol, endRow, endCol);
    }

    private boolean applySimpleMove(Piece[][] board, String uci) {
        MoveRequest m = uciToMove(uci);
        Piece piece = board[m.startRow][m.startCol];
        if (piece == null) return false;

        if (piece instanceof King && Math.abs(m.endCol - m.startCol) == 2) return false;
        if (piece instanceof Pawn && m.startCol != m.endCol && board[m.endRow][m.endCol] == null) return false;

        board[m.endRow][m.endCol] = piece;
        board[m.startRow][m.startCol] = null;
        piece.setX(m.endRow);
        piece.setY(m.endCol);
        return true;
    }

    private String describeTheme(Context context, int number, String themes) {
        // 1) Matti "con nome": titolo dedicato
        if (themes.contains("smotheredMate"))    return context.getString(R.string.theme_smothered_mate);
        if (themes.contains("arabianMate"))      return context.getString(R.string.theme_arabian_mate);
        if (themes.contains("anastasiaMate"))    return context.getString(R.string.theme_anastasia_mate);
        if (themes.contains("bodenMate"))        return context.getString(R.string.theme_boden_mate);
        if (themes.contains("backRankMate"))     return context.getString(R.string.theme_back_rank_mate);
        if (themes.contains("hookMate"))         return context.getString(R.string.theme_hook_mate);
        if (themes.contains("doubleBishopMate")) return context.getString(R.string.theme_double_bishop_mate);
        if (themes.contains("dovetailMate"))     return context.getString(R.string.theme_dovetail_mate);

        // 2) Motivi tattici
        if (themes.contains("sacrifice"))        return context.getString(R.string.theme_sacrifice);
        if (themes.contains("doubleCheck"))      return context.getString(R.string.theme_double_check);
        if (themes.contains("deflection"))       return context.getString(R.string.theme_deflection);
        if (themes.contains("attraction"))       return context.getString(R.string.theme_attraction);
        if (themes.contains("discoveredAttack")) return context.getString(R.string.theme_discovered_attack);
        if (themes.contains("promotion"))        return context.getString(R.string.theme_promotion);

        // 3) Fallback pool
        int[] pool;
        if (themes.contains("mateIn1")) {
            pool = new int[]{R.string.pool_sharp_blow, R.string.pool_single_move, R.string.pool_final_blow,
                    R.string.pool_lightning_execution, R.string.pool_immediate_victory};
        } else if (themes.contains("mateIn2")) {
            pool = new int[]{R.string.pool_trap, R.string.pool_two_moves_glory, R.string.pool_ambush,
                    R.string.pool_net_closes, R.string.pool_king_hunt, R.string.pool_perfect_plan};
        } else if (themes.contains("mateIn3")) {
            pool = new int[]{R.string.pool_long_hunt, R.string.pool_three_moves_triumph, R.string.pool_siege,
                    R.string.pool_spider_web, R.string.pool_final_check};
        } else {
            pool = new int[]{R.string.pool_winning_tactic, R.string.pool_right_moment, R.string.pool_watch_king};
        }
        return context.getString(pool[number % pool.length]);
    }
}