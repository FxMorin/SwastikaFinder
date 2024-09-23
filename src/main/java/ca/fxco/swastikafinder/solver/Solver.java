package ca.fxco.swastikafinder.solver;

import ca.fxco.swastikafinder.Main;
import ca.fxco.swastikafinder.chunk.Region;
import ca.fxco.swastikafinder.chunk.SubChunk;

import java.util.ArrayList;

/**
 * Solves for a pattern within a {@link Region}.
 * This is multithreaded to account for lookup costs when used in other systems.
 *
 * @author FX
 */
public class Solver {

    public void solve(Region region, byte[] blockIds) {
        ArrayList<ThreadedSolver> threads = new ArrayList<>();
        for (int i = 0; i < Main.THREADS; i++) {
            threads.add(new ThreadedSolver(region));
        }
        int currentId = 0;
        for (byte x = 0; x < 16; x++) {
            short startX = (short) (x << 4);
            for (byte z = 0; z < 16; z++) {
                short startZ = (short) (z << 4);
                ThreadedSolver thread = threads.get(currentId++);
                thread.setup(x, z, startX, startZ, blockIds);
                thread.smartStart();
                if (currentId >= Main.THREADS) {
                    currentId = 0;
                    for (ThreadedSolver thread2 : threads) {
                        try {
                            thread2.join();
                        } catch (InterruptedException ignored) {}
                    }
                }
            }
        }
    }

    public static class ThreadedSolver extends Thread {

        private final ChunkSolver chunkSolver = new ChunkSolver();
        private final Region region;
        private byte x;
        private byte z;
        private short startX;
        private short startZ;
        private byte[] blockIds;
        private volatile boolean hasStarted = false;

        public ThreadedSolver(Region region) {
            this.region = region;
        }

        public void setup(byte x, byte z, short startX, short startZ, byte[] blockIds) {
            this.x = x;
            this.z = z;
            this.startX = startX;
            this.startZ = startZ;
            this.blockIds = blockIds;
        }

        public void smartStart() {
            if (!hasStarted) {
                hasStarted = true;
                this.start();
            }
            for (byte y = 0; y < 16; y++) {
                short startY = (short) (y << 4);
                SubChunk chunk = region.getChunk(x, y, z);
                for (byte blockId : blockIds) {
                    if (chunk.doesPaletteContain(blockId)) {
                        chunkSolver.solveChunk(region, startX, startY, startZ, blockId);
                    }
                }
            }
        }

        @Override
        public void run() {}
    }
}
