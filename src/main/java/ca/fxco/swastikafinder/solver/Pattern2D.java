package ca.fxco.swastikafinder.solver;

/**
 * Solves swastika's of any size within a 2D Plane.
 *
 * @author FX
 */
public class Pattern2D {

    public static boolean solveSwastika2D(boolean[][] grid, int totalCount) {
        // Setup values
        int evenOffset = grid.length % 2 ^ 1;
        int center = (grid.length / 2) - evenOffset;

        // Find Thickness
        int thickness = -1;
        int maxThickness = grid.length - 4;
        for (int g = 0; g < maxThickness; g++) {
            if (!grid[center + evenOffset + g][center + evenOffset + g]) {
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
        if (grid[0][center + evenOffset + thickness + 1]) {
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
            for (int y = center + evenOffset + thickness + 2; y < grid.length; y++) {
                if (grid[0][y]) {
                    hookHeight++;
                } else {
                    break;
                }
            }
        }
        // Find hook length
        int hookLength = 1;
        int max = center + (reverseHook ? 0 : evenOffset) + (thickness + 1) * (reverseHook ? -1 : 1);
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
            for (int y = center - thickness; y <= center + evenOffset + thickness; y++) {
                if (!line[y]) {
                    return false;
                }
            }
        }
        // Vertical - done in 2 steps to avoid checking the center positions again
        for (int y = 0; y < center - thickness; y++) { // top
            for (int x = center - thickness; x <= center + evenOffset + thickness; x++) {
                if (!grid[x][y]) {
                    return false;
                }
            }
        }
        for (int y = center + thickness; y < grid.length; y++) { // bottom
            for (int x = center - thickness; x <= center + evenOffset + thickness; x++) {
                if (!grid[x][y]) {
                    return false;
                }
            }
        }

        // Check if area matches the total
        int absThickness = (thickness * 2) + 1 + evenOffset;
        int crossArea = grid.length * absThickness * 2 - absThickness * absThickness;
        int hookArea = hookHeight * hookLength * 4;
        if (crossArea + hookArea != totalCount) {
            return false;
        }

        // Check Hooks
        // Normal Top & Reverse Left hook
        for (int x = 0; x < hookLength; x++) {
            for (int y = center + evenOffset + thickness; y <= center + evenOffset + thickness + hookHeight; y++) {
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
            for (int x = center + evenOffset + thickness; x <= center + evenOffset + thickness + hookHeight; x++) {
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
}
