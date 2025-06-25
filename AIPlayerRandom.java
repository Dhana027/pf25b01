/**
 * AIPlayerRandom (Level Easy)
 * AI ini hanya akan memilih langkah secara acak dari kotak yang kosong.
 * Menggunakan Math.random() untuk meminimalkan import.
 *
 * Komentar dalam Bahasa Indonesia untuk kemudahan pemahaman.
 */
public class AIPlayerRandom extends AIPlayer {

    /** constructor */
    public AIPlayerRandom(Board board) {
        super(board);
    }

    /**
     * Mengembalikan gerakan acak.
     * @return int[2] dari {baris, kolom}
     */
    @Override
    public int[] move() {
        int row, col;
        // Terus mencari koordinat acak sampai menemukan yang kosong
        do {
            // Math.random() menghasilkan double antara 0.0 dan < 1.0
            // Dikalikan 3 akan menghasilkan 0.0 sampai < 3.0
            // Di-cast ke (int) akan menghasilkan 0, 1, atau 2.
            row = (int)(Math.random() * ROWS);
            col = (int)(Math.random() * COLS);
        } while (cells[row][col].content != Seed.NO_SEED);

        return new int[]{row, col};
    }
}
