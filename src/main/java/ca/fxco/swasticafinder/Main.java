package ca.fxco.swasticafinder;

import ca.fxco.swasticafinder.chunk.Region;
import ca.fxco.swasticafinder.chunk.SubChunk;
import ca.fxco.swasticafinder.solver.Solver;

import java.util.concurrent.TimeUnit;

public class Main {

    public static final int CHUNK_AMOUNT = 4096;
    public static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        System.out.println("Starting Swastica Finder...");
        System.out.println("Finding with " + THREADS + " threads");
        System.out.println("Creating region");
        Region region = new Region();
        System.out.println("Test #1");
        test1(region);
        System.out.println("Test #2");
        test2(region);
        System.out.println("Test #3");
        test3(region);
        //createSwastika2D(5, 0, 1, 1, false);  // Simple
        //createSwastika2D(11, 1, 2, 2, false); // Weird
        //createSwastika2D(11, 1, 4, 3, false); // Full
        //createSwastika2D(6, 0, 1, 1, false);  // Simple Even
    }

    // Test against random data
    public static void test1(Region region) {
        region.populate();
        Solver solver = new Solver();
        solver.solve(region, new byte[]{9});
    }

    // There is a swastica in this mess, find it
    public static void test2(Region region) {
        region.populate();
        region.setChunk((byte) 12, (byte) 3, (byte) 8, createSwasticaChunk());
        Solver solver = new Solver();
        long start = System.nanoTime();
        solver.solve(region, new byte[]{9});
        System.out.println(
                "Solver took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) +
                "ms to scan " + (CHUNK_AMOUNT * 4096) + " blocks in " + CHUNK_AMOUNT + " chunks"
        ); // 4096 blocks in a chunk
    }

    public static void test3(Region region) {
        region.populate();
        region.setChunk((byte) 9, (byte) 12, (byte) 14, createThickSwasticaChunk());
        Solver solver = new Solver();
        long start = System.nanoTime();
        solver.solve(region, new byte[]{9});
        System.out.println(
                "Solver took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) +
                "ms to scan " + (CHUNK_AMOUNT * 4096) + " blocks in " + CHUNK_AMOUNT + " chunks"
        ); // 4096 blocks in a chunk
    }

    public static void test99(Region region) {
        SubChunk swasticaChunk = createSwasticaChunk();
        for (byte x = 0; x < 16; x++) {
            for (byte y = 0; y < 16; y++) {
                for (byte z = 0; z < 16; z++) {
                    region.setChunk(x, y, z, swasticaChunk);
                }
            }
        }
        Solver solver = new Solver();
        long start = System.nanoTime();
        solver.solve(region, new byte[]{9});
        System.out.println(
                "Solver took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) +
                "ms to scan " + (CHUNK_AMOUNT * 4096) + " blocks in " + CHUNK_AMOUNT + " chunks"
        ); // 4096 blocks in a chunk
    }

    private static SubChunk createSwasticaChunk() {
        SubChunk chunk = new SubChunk();
        byte blockId = (byte) 9;
        byte z = (byte) 7;
        boolean[][] grid = new boolean[5][5];
        populateSwastika2D(grid, 0, 1, 1, false);
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {
                if (grid[x][y]) {
                    chunk.setBlock((byte) (x + 2), (byte) (y + 1), z, blockId);
                }
            }
        }
        return chunk;
    }

    private static SubChunk createThickSwasticaChunk() {
        boolean[][] grid = new boolean[6][6];
        populateSwastika2D(grid, 0, 2, 1, false);
        SubChunk chunk = new SubChunk();
        byte blockId = (byte) 9;
        byte z = (byte) 7;
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {
                if (grid[x][y]) {
                    chunk.setBlock((byte) (x + 2), (byte) (y + 1), z, blockId);
                }
            }
        }
        return chunk;
    }

    private static void createSwastika2D(int size, int thickness, int hookLength, int hookHeight,
                                         boolean reverseHook) {
        boolean[][] grid = new boolean[size][size];
        populateSwastika2D(grid, thickness, hookLength, hookHeight, reverseHook);
        for (boolean[] line : grid) {
            StringBuilder builder = new StringBuilder();
            for (boolean p : line) {
                builder.append(p ? "#" : " ");
            }
            System.out.println(builder);
        }
    }

    // This is basically the solve in reverse
    public static void populateSwastika2D(boolean[][] grid, int thickness, int hookLength, int hookHeight,
                                          boolean reverseHook) {
        // Setup values
        int oddOffset = grid.length % 2 ^ 1;
        int center = (grid.length / 2) - oddOffset;

        // Create cross
        // Horizontal
        for (int x = 0; x < grid.length; x++) {
            for (int y = center - thickness; y <= center + oddOffset + thickness; y++) {
                grid[x][y] = true;
            }
        }
        // Vertical - done in 2 steps to avoid checking the center positions again
        for (int y = 0; y <= center - thickness; y++) { // top
            for (int x = center - thickness; x <= center + oddOffset + thickness; x++) {
                grid[x][y] = true;
            }
        }
        for (int y = center + oddOffset + thickness; y < grid.length; y++) { // bottom
            for (int x = center - thickness; x <= center + oddOffset + thickness; x++) {
                grid[x][y] = true;
            }
        }

        // Create Hooks
        // Normal Top & Reverse Left hook
        for (int x = 0; x < hookHeight; x++) {
            for (int y = center + oddOffset + thickness; y <= (center + oddOffset + thickness) + hookLength; y++) {
                if (reverseHook) {
                    grid[y][x] = true;
                } else {
                    grid[x][y] = true;
                }
            }
        }
        // Normal Left & Reverse Top hook
        for (int y = 0; y < hookHeight; y++) {
            for (int x = (center - thickness) - hookLength; x <= center - thickness; x++) {
                if (reverseHook) {
                    grid[y][x] = true;
                } else {
                    grid[x][y] = true;
                }
            }
        }
        // Normal Bottom & Reverse Right hook
        for (int y = grid.length - hookHeight; y < grid.length; y++) {
            for (int x = center + oddOffset + thickness; x <= (center + oddOffset + thickness) + hookLength; x++) {
                if (reverseHook) {
                    grid[y][x] = true;
                } else {
                    boolean[] xx = grid[x];
                    xx[y] = true;
                }
            }
        }
        // Normal Right & Reverse Bottom hook
        for (int x = grid.length - hookHeight; x < grid.length; x++) {
            for (int y = (center - thickness) - hookLength; y <= center - thickness; y++) {
                if (reverseHook) {
                    grid[y][x] = true;
                } else {
                    grid[x][y] = true;
                }
            }
        }
    }
}