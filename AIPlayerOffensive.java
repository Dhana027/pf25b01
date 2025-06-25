/**
 * AIPlayerOffensive (Level Medium)
 * AI ini hanya akan memprioritaskan langkah yang akan membuatnya menang.
 * Ia tidak peduli untuk memblokir lawan.
 *
 * Komentar dalam Bahasa Indonesia untuk kemudahan pemahaman.
 */
public class AIPlayerOffensive extends AIPlayer {

    // Daftar prioritas langkah (rencana default jika tidak ada langkah kemenangan)
    private int[][] preferredMoves = {
            {1, 1}, {0, 0}, {0, 2}, {2, 0}, {2, 2},
            {0, 1}, {1, 0}, {1, 2}, {2, 1}};

    /** constructor */
    public AIPlayerOffensive(Board board) {
        super(board);
    }

    /**
     * Mengembalikan gerakan berdasarkan prioritas:
     * 1. Mencari langkah untuk menang.
     * 2. Jika tidak ada, ikuti tabel prioritas.
     */
    @Override
    public int[] move() {
        // Prioritas 1: Cari langkah untuk menang.
        int[] winningMove = findWinningMove(mySeed);
        if (winningMove != null) {
            return winningMove; // Ambil kemenangan
        }

        // Prioritas 2: Jika tidak ada, ikuti daftar prioritas.
        for (int[] move : preferredMoves) {
            if (cells[move[0]][move[1]].content == Seed.NO_SEED) {
                return move;
            }
        }

        return null; // Seharusnya tidak terjadi jika papan belum penuh
    }

    /**
     * Metode untuk mencari langkah kemenangan untuk pemain tertentu.
     * @param player Bidak pemain (hanya mySeed yang akan digunakan di sini).
     * @return Koordinat {baris, kolom} dari langkah kemenangan, atau null.
     */
    private int[] findWinningMove(Seed player) {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (cells[row][col].content == Seed.NO_SEED) {
                    // Coba lakukan gerakan sementara
                    cells[row][col].content = player;
                    if (hasWon(player, row, col)) {
                        cells[row][col].content = Seed.NO_SEED; // Batalkan
                        return new int[]{row, col};
                    }
                    cells[row][col].content = Seed.NO_SEED; // Batalkan
                }
            }
        }
        return null;
    }

    /**
     * Metode bantuan untuk memeriksa kemenangan.
     */
    private boolean hasWon(Seed player, int currentRow, int currentCol) {
        return (cells[currentRow][0].content == player && cells[currentRow][1].content == player && cells[currentRow][2].content == player) ||
                (cells[0][currentCol].content == player && cells[1][currentCol].content == player && cells[2][currentCol].content == player) ||
                (currentRow == currentCol && cells[0][0].content == player && cells[1][1].content == player && cells[2][2].content == player) ||
                (currentRow + currentCol == 2 && cells[0][2].content == player && cells[1][1].content == player && cells[2][0].content == player);
    }
}