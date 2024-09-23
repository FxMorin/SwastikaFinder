import ca.fxco.swasticafinder.Main;
import ca.fxco.swasticafinder.solver.Pattern2D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test2DSolver {

    @Test
    public void testSolveSimplestOdd() {
        // Normal
        boolean[][] grid = new boolean[5][5];
        Main.populateOddSwastika2D(grid, 0, 1, 1, false);
        Assertions.assertTrue(Pattern2D.solveOddSwastika2D(grid, 13));

        // Reverse
        grid = new boolean[5][5];
        Main.populateOddSwastika2D(grid, 0, 1, 1, true);
        Assertions.assertTrue(Pattern2D.solveOddSwastika2D(grid, 13));
    }

    @Test
    public void testSolveWeirdOdd() {
        // Normal
        boolean[][] grid = new boolean[11][11];
        Main.populateOddSwastika2D(grid, 1, 2, 2, false);
        Assertions.assertTrue(Pattern2D.solveOddSwastika2D(grid, 73));

        // Reverse
        grid = new boolean[11][11];
        Main.populateOddSwastika2D(grid, 1, 2, 2, true);
        Assertions.assertTrue(Pattern2D.solveOddSwastika2D(grid, 73));
    }

    @Test
    public void testSolveFullOdd() {
        // Normal
        boolean[][] grid = new boolean[11][11];
        Main.populateOddSwastika2D(grid, 1, 4, 3, false);
        Assertions.assertTrue(Pattern2D.solveOddSwastika2D(grid, 105));

        // Reverse
        grid = new boolean[11][11];
        Main.populateOddSwastika2D(grid, 1, 4, 3, true);
        Assertions.assertTrue(Pattern2D.solveOddSwastika2D(grid, 105));
    }
}
