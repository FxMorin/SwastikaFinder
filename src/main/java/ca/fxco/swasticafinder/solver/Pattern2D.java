package ca.fxco.swasticafinder.solver;

public class Pattern2D {

    public static boolean solveSwastika2D(boolean[][] grid, int totalCount) { // get size
        if (grid.length % 2 == 1) {
            return solveOddSwastika2D(grid, totalCount);
        } else {
            return solveEvenSwastika2D(grid);
        }
    }

    public static boolean solveOddSwastika2D(boolean[][] grid, int totalCount) {
        // Setup values
        int center = grid.length / 2;

        // Find Thickness
        int thickness = -1;
        int maxThickness = grid.length - 4;
        for (int g = 0; g < maxThickness; g++) {
            if (!grid[center + g][center + g]) {
                // Test if this is an illegal thickness for an early exit
                if (g == maxThickness - 1 && !grid[center + g + 1][center + g + 1]) {
                    return false;
                }
                thickness = g;
                break;
            }
        }
        if (thickness == -1) {
            thickness = maxThickness;
        }
        if (thickness == 0) { // No center
            return false;
        }
        thickness--;

        // Find hook direction
        boolean reverseHook = true;
        if (grid[0][center + thickness + 1]) {
            if (grid[0][center - thickness - 1]) {
                return false; // Hook is on both sides...
            }
            reverseHook = false;
        } else if (!grid[0][center - thickness - 1]) {
            return false; // Not left
        }

        // Find hook height
        int hookHeight = 1;
        if (reverseHook) {
            for (int y = center - thickness - 2; y >= 0; y--) {
                if (grid[0][y]) {
                    hookHeight++;
                } else {
                    break;
                }
            }
        } else {
            for (int y = center + thickness + 2; y < grid.length; y++) {
                if (grid[0][y]) {
                    hookHeight++;
                } else {
                    break;
                }
            }
        }
        // Find hook length
        int hookLength = 1;
        int max = center + (thickness + 1) * (reverseHook ? -1 : 1);
        for (int x = 1; x < max; x++) {
            if (grid[x][max]) {
                hookLength++;
            } else {
                break;
            }
        }

        // Check cross
        // Horizontal
        for (boolean[] line : grid) {
            for (int y = center - thickness; y <= center + thickness; y++) {
                if (!line[y]) {
                    return false;
                }
            }
        }
        // Vertical - done in 2 steps to avoid checking the center positions again
        for (int y = 0; y < center - thickness; y++) { // top
            for (int x = center - thickness; x <= center + thickness; x++) {
                if (!grid[x][y]) {
                    return false;
                }
            }
        }
        for (int y = center + thickness; y < grid.length; y++) { // bottom
            for (int x = center - thickness; x <= center + thickness; x++) {
                if (!grid[x][y]) {
                    return false;
                }
            }
        }

        // Check if area matches the total
        int absThickness = (thickness * 2) + 1;
        int crossArea = grid.length * absThickness * 2 - absThickness * absThickness;
        int hookArea = hookHeight * hookLength * 4;
        if (crossArea + hookArea != totalCount) {
            return false;
        }

        // Check Hooks
        // Normal Top & Reverse Left hook
        for (int x = 0; x < hookLength; x++) {
            for (int y = center + thickness; y <= (center + thickness) + hookHeight; y++) {
                if (!(reverseHook ? grid[y][x] : grid[x][y])) {
                    return false;
                }
            }
        }
        // Normal Left & Reverse Top hook
        for (int y = 0; y < hookLength; y++) {
            for (int x = (center - thickness) - hookHeight; x <= center - thickness; x++) {
                if (!(reverseHook ? grid[y][x] : grid[x][y])) {
                    return false;
                }
            }
        }
        // Normal Bottom & Reverse Right hook
        for (int y = grid.length - hookLength; y < grid.length; y++) {
            for (int x = center + thickness; x <= (center + thickness) + hookHeight; x++) {
                if (!(reverseHook ? grid[y][x] : grid[x][y])) {
                    return false;
                }
            }
        }
        // Normal Right & Reverse Bottom hook
        for (int x = grid.length - hookLength; x < grid.length; x++) {
            for (int y = (center - thickness) - hookHeight; y <= center - thickness; y++) {
                if (!(reverseHook ? grid[y][x] : grid[x][y])) {
                    return false;
                }
            }
        }

        return true; // Valid swastika
    }

    public static boolean solveEvenSwastika2D(boolean[][] group) {
        return true;
    }

}
