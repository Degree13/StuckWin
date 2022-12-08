import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;

public class CreateAndWriteCSV {
    public static void main(String[] args) {
        try {
            // Create a two-dimensional array representing the board game
            char[][] board = {{'X', 'O', 'X'},
                              {'O', 'X', 'O'},
                              {'X', 'O', 'X'}};

            // Create a variable to store the winner ('R' or 'B')
            char winner = 'R';

            // Create a File object for the CSV file
            File csvFile = new File("myfile.csv");

            // Create a PrintWriter to write the CSV file
            PrintWriter pw = new PrintWriter(csvFile);

            // If the file does not exist, create it and write the header row
            if (!csvFile.exists()) {
                pw.println("Board,Winner");
            }

            // Loop through the board array and write each row to the CSV file
            for (int i = 0; i < board.length; i++) {
                // Convert the row array to a string and write it to the file
                pw.println(Arrays.toString(board[i]) + "," + winner);
            }

            // Close the PrintWriter
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
