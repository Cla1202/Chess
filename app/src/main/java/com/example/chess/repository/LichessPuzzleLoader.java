package com.example.chess.repository;

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
 *
 * Formato riga CSV:
 * PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl,OpeningTags
 *
 * NOTA: nel formato Lichess la FEN rappresenta la posizione PRIMA della mossa
 * dell'avversario. La prima mossa del campo "Moves" viene applicata alla
 * scacchiera; le mosse rimanenti costituiscono la soluzione (alternando
 * giocatore e avversario, come nel formato gia' usato da QuizRepository).
 *
 * Convenzione coordinate (identica a QuizRepository):
 *   x = riga (0 = ottava traversa, 7 = prima traversa)
 *   y = colonna (0 = colonna 'a', 7 = colonna 'h')
 */
public class LichessPuzzleLoader {
    private final Map<String, Integer> titleCounts = new HashMap<>();

    /**
     * Legge un CSV (es. da assets) e restituisce i livelli filtrati.
     *
     * @param input        stream del file CSV (decompresso)
     * @param themeFilter  tema richiesto, es. "mateIn1", "mateIn2" (null = tutti)
     * @param minRating    rating minimo del puzzle (es. 600)
     * @param maxRating    rating massimo del puzzle (es. 1200)
     * @param maxLevels    numero massimo di livelli da caricare
     */
    public List<QuizLevel> loadFromCsv(InputStream input,
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

                QuizLevel level = buildLevel(levelNumber, fen, movesUci, themes);
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
     * Applica la prima mossa (quella dell'avversario) alla scacchiera,
     * le restanti diventano la soluzione.
     */
    private QuizLevel buildLevel(int number, String fen, String movesUci, String themes) {
        Piece[][] board = parseFen(fen);
        if (board == null) return null;

        String[] uciMoves = movesUci.trim().split("\\s+");
        if (uciMoves.length < 2) return null;

        // Scartiamo puzzle con promozioni (mosse UCI a 5 caratteri, es. e7e8q):
        // QuizLevel/MoveRequest non hanno ancora il concetto di promozione.
        for (String m : uciMoves) {
            if (m.length() != 4) return null;
        }

        // La prima mossa e' dell'avversario: la applichiamo alla scacchiera.
        if (!applySimpleMove(board, uciMoves[0])) return null;

        // Chi muove dopo la prima mossa e' l'opposto del side-to-move della FEN.
        boolean whiteToMoveInFen = fen.split(" ")[1].equals("w");
        boolean playerIsWhite = !whiteToMoveInFen;

        List<MoveRequest> soluzione = new ArrayList<>();
        for (int i = 1; i < uciMoves.length; i++) {
            soluzione.add(uciToMove(uciMoves[i]));
        }

        String baseTitle = describeTheme(number, themes);
        int count = titleCounts.getOrDefault(baseTitle, 0) + 1;
        titleCounts.put(baseTitle, count);
        String title = "Livello " + number + ": " + baseTitle
                + (count > 1 ? " #" + count : "");
        return new QuizLevel(number, title, board, playerIsWhite, soluzione, 3);
    }

    // ==========================================
    // FEN PARSING
    // ==========================================

    /** Converte la parte "piece placement" di una FEN in Piece[][] (riga 0 = ottava traversa). */
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

    // ==========================================
    // UCI MOVES
    // ==========================================

    /** "e2e4" -> MoveRequest, stessa convenzione di QuizRepository.mossa(). */
    public MoveRequest uciToMove(String uci) {
        int startCol = uci.charAt(0) - 'a';
        int startRow = 8 - Character.getNumericValue(uci.charAt(1));
        int endCol = uci.charAt(2) - 'a';
        int endRow = 8 - Character.getNumericValue(uci.charAt(3));
        return new MoveRequest(startRow, startCol, endRow, endCol);
    }

    /**
     * Applica una mossa UCI alla scacchiera in modo "ingenuo":
     * sposta il pezzo, cattura quello di destinazione e aggiorna
     * le coordinate interne del pezzo (x = riga, y = colonna).
     * Non gestisce arrocco, en passant, promozione: i puzzle che
     * li richiedono nella prima mossa vengono scartati.
     */
    private boolean applySimpleMove(Piece[][] board, String uci) {
        MoveRequest m = uciToMove(uci);
        Piece piece = board[m.startRow][m.startCol];
        if (piece == null) return false;

        // Scarta arrocco (re che si muove di 2 colonne)
        if (piece instanceof King && Math.abs(m.endCol - m.startCol) == 2) {
            return false;
        }
        // Scarta possibile en passant (pedone che cattura in diagonale su casa vuota)
        if (piece instanceof Pawn
                && m.startCol != m.endCol
                && board[m.endRow][m.endCol] == null) {
            return false;
        }

        board[m.endRow][m.endCol] = piece;
        board[m.startRow][m.startCol] = null;

        // Tiene coerenti le coordinate interne del pezzo con l'array
        piece.setX(m.endRow);
        piece.setY(m.endCol);
        return true;
    }

    // ==========================================
    // UTILS
    // ==========================================

    private String describeTheme(int number, String themes) {
        // 1) Matti "con nome": titolo dedicato (priorità ai più specifici)
        if (themes.contains("smotheredMate"))    return "Matto Affogato";
        if (themes.contains("arabianMate"))      return "Matto Arabo";
        if (themes.contains("anastasiaMate"))    return "Matto di Anastasia";
        if (themes.contains("bodenMate"))        return "Matto di Boden";
        if (themes.contains("backRankMate"))     return "Matto del Corridoio";
        if (themes.contains("hookMate"))         return "Matto dell'Uncino";
        if (themes.contains("doubleBishopMate")) return "I Due Alfieri";
        if (themes.contains("dovetailMate"))     return "Coda di Rondine";

        // 2) Motivi tattici notevoli
        if (themes.contains("sacrifice"))        return "Il Sacrificio";
        if (themes.contains("doubleCheck"))      return "Doppio Scacco";
        if (themes.contains("deflection"))       return "La Deviazione";
        if (themes.contains("attraction"))       return "L'Esca";
        if (themes.contains("discoveredAttack")) return "Attacco di Scoperta";
        if (themes.contains("promotion"))        return "L'Ottava Traversa";

        // 3) Fallback: rosa di titoli che ruota col numero del livello
        String[] pool;
        if (themes.contains("mateIn1")) {
            pool = new String[]{"Colpo Secco", "Una Sola Mossa", "Il Colpo di Grazia",
                    "Esecuzione Lampo", "Vittoria Immediata"};
        } else if (themes.contains("mateIn2")) {
            pool = new String[]{"La Trappola", "Due Mosse alla Gloria", "L'Imboscata",
                    "La Rete si Chiude", "Caccia al Re", "Il Piano Perfetto"};
        } else if (themes.contains("mateIn3")) {
            pool = new String[]{"La Lunga Caccia", "Tre Mosse al Trionfo", "L'Assedio",
                    "La Tela del Ragno", "Scacco Finale"};
        } else {
            pool = new String[]{"Tattica Vincente", "Il Momento Giusto", "Occhio al Re"};
        }
        return pool[number % pool.length];
    }
}
