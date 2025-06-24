/**
 * AI yang lebih pintar. Ia akan mencoba menang, lalu mencoba memblokir,
 * sebelum akhirnya mengikuti tabel prioritas.
 */
public class AIPlayerTableLookup extends AIPlayer {

    // Daftar prioritas langkah (rencana default)
    private int[][] preferredMoves = {
            {1, 1}, {0, 0}, {0, 2}, {2, 0}, {2, 2},
            {0, 1}, {1, 0}, {1, 2}, {2, 1}};

    /** constructor */
    public AIPlayerTableLookup(Board board) {
        super(board);
    }

    /**
     * PERUBAHAN: Logika baru dengan prioritas menang dan bertahan.
     */
    @Override
    public int[] move() {
        // Prioritas 1: Cari langkah untuk menang.
        int[] winningMove = findWinningMove(mySeed);
        if (winningMove != null) {
            return winningMove; // Ambil kemenangan!
        }

        // Prioritas 2: Cari langkah untuk memblokir lawan.
        int[] blockingMove = findWinningMove(oppSeed);
        if (blockingMove != null) {
            return blockingMove; // Blokir lawan!
        }

        // Prioritas 3: Jika tidak ada yang darurat, ikuti daftar prioritas.
        for (int[] move : preferredMoves) {
            if (cells[move[0]][move[1]].content == Seed.NO_SEED) {
                return move;
            }
        }

        // Seharusnya tidak pernah sampai ke baris ini.
        assert false : "No empty cell?!";
        return null;
    }

    /**
     * BARU: Metode untuk mencari langkah kemenangan untuk pemain tertentu.
     * @param player Bidak pemain (mySeed atau oppSeed) yang ingin dicek.
     * @return Koordinat {baris, kolom} dari langkah kemenangan, atau null jika tidak ada.
     */
    private int[] findWinningMove(Seed player) {
        // Iterasi ke semua sel yang kosong
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (cells[row][col].content == Seed.NO_SEED) {
                    // Coba lakukan gerakan sementara
                    cells[row][col].content = player;
                    // Cek apakah gerakan ini membuat pemain menang
                    if (hasWon(player, row, col)) {
                        // Jika ya, batalkan gerakan sementara dan kembalikan posisi ini
                        cells[row][col].content = Seed.NO_SEED;
                        return new int[]{row, col};
                    }
                    // Batalkan gerakan sementara jika tidak menang
                    cells[row][col].content = Seed.NO_SEED;
                }
            }
        }
        return null; // Tidak ada gerakan kemenangan yang ditemukan
    }

    /**
     * BARU: Metode bantuan untuk memeriksa apakah pemain menang setelah
     * menempatkan bidak di (currentRow, currentCol).
     */
    private boolean hasWon(Seed player, int currentRow, int currentCol) {
        // Logika ini sama dengan yang ada di kelas Board
        return (cells[currentRow][0].content == player && cells[currentRow][1].content == player && cells[currentRow][2].content == player) ||
                (cells[0][currentCol].content == player && cells[1][currentCol].content == player && cells[2][currentCol].content == player) ||
                (currentRow == currentCol && cells[0][0].content == player && cells[1][1].content == player && cells[2][2].content == player) ||
                (currentRow + currentCol == 2 && cells[0][2].content == player && cells[1][1].content == player && cells[2][0].content == player);
    }
}