package ca.fxco.swasticafinder;

import ca.fxco.swasticafinder.chunk.Region;
import ca.fxco.swasticafinder.chunk.SubChunk;
import ca.fxco.swasticafinder.solver.Solver;

import java.util.concurrent.TimeUnit;

public class Main {

    public static final int MAX_SWASTIKA_WIDTH = 4;
    public static final int MAX_DEPTH = 1000 * MAX_SWASTIKA_WIDTH;
    public static final int CHUNK_AMOUNT = 4096;
    public static final int THREADS = Runtime.getRuntime().availableProcessors();

    private static final byte[][] swastica = new byte[][] {
            //x, y
            {0, 4},
            {0, 3},
            {1, 3},
            {2, 3},
            {3, 3}, // center
            {4, 3},
            {5, 3},
            {6, 3},
            {6, 2}, // End of horizontal line
            {2, 0},
            {3, 0},
            {3, 1},
            {3, 2},
            {3, 4},
            {3, 5},
            {3, 6},
            {4, 6}
    };

    public static void main(String[] args) {
        System.out.println("Starting Swastica Finder...");
        System.out.println("Finding with " + THREADS + " threads");
        System.out.println("Creating region");
        Region region = new Region();
        System.out.println("Test #1");
        test1(region);
        System.out.println("Test #2");
        test2(region);
        //for (int i = 0; i < 50; i++) {
        //    test2(region);
        //}
        //test2(region);
        //System.out.println("Test #3");
        //test3(region);
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
        System.out.println("Solver took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms to scan " + (CHUNK_AMOUNT * 4096) + " blocks in " + CHUNK_AMOUNT + " chunks"); // 4096 blocks in a chunk
    }

    public static void test3(Region region) {
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
        System.out.println("Solver took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms to scan " + (CHUNK_AMOUNT * 4096) + " blocks in " + CHUNK_AMOUNT + " chunks"); // 4096 blocks in a chunk
    }

    private static SubChunk createSwasticaChunk() {
        SubChunk chunk = new SubChunk();
        byte blockId = (byte) 9;
        byte z = (byte) 7;
        for (byte[] pos : swastica) {
            chunk.setBlock((byte) (pos[0] + 2), (byte) (pos[1] + 1), z, blockId);
        }
        return chunk;
    }
}