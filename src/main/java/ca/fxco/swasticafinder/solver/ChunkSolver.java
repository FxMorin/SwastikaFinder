package ca.fxco.swasticafinder.solver;

import ca.fxco.swasticafinder.chunk.Region;
import ca.fxco.swasticafinder.chunk.SubChunk;
import ca.fxco.swasticafinder.util.AABB;
import ca.fxco.swasticafinder.util.Axis;
import ca.fxco.swasticafinder.util.Direction;
import ca.fxco.swasticafinder.util.PosUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Chunk solvers are reused for multiple chunks. There is one chunk solver per thread
 *
 * @author FX
 */
public class ChunkSolver {

    private static final int SMALLEST_POSSIBLE_SWASTIKA = 13;

    private final Queue<CarriedData> queue = new ArrayDeque<>();
    private final IntSet positions = new IntOpenHashSet(40);
    private final IntSet currentGroup = new IntOpenHashSet(18);

    public void solveChunk(Region region, short startX, short startY, short startZ, byte blockId) {
        positions.clear();
        queue.clear();
        currentGroup.clear();
        int chunkMaxX = startX + 16;
        int chunkMaxY = startY + 16;
        int chunkMaxZ = startZ + 16;
        for (short x = startX; x < chunkMaxX; x++) {
            for (short y = startY; y < chunkMaxY; y++) {
                for (short z = startZ; z < chunkMaxZ; z++) {
                    if (optimizedBFT(region, x, y, z, blockId) >= SMALLEST_POSSIBLE_SWASTIKA) {
                        analyzeGroup();
                    }
                }
            }
        }
    }

    private int optimizedBFT(Region region, short x, short y, short z, byte blockId) {
        int startPos = PosUtil.toInt(x, y, z);
        if (positions.contains(startPos)) {
            return 0;
        }
        byte chunkX = (byte) (x >> 4);
        byte chunkY = (byte) (y >> 4);
        byte chunkZ = (byte) (z >> 4);
        currentGroup.clear();
        queue.add(new CarriedData(startPos, null));
        int count = 0;

        while(!queue.isEmpty()) {
            CarriedData pair = queue.poll();
            int pos = pair.getKey();
            if (positions.add(pos)) {
                short nextX = PosUtil.getX(pos);
                short nextY = PosUtil.getY(pos);
                short nextZ = PosUtil.getZ(pos);
                byte nextChunkX = (byte) (nextX >> 4);
                byte nextChunkY = (byte) (nextY >> 4);
                byte nextChunkZ = (byte) (nextZ >> 4);
                SubChunk nextChunk = region.getChunk(nextChunkX, nextChunkY, nextChunkZ);
                if (nextChunk.getBlock((byte) (nextX & 15), (byte) (nextY & 15), (byte) (nextZ & 15)) != blockId) {
                    continue;
                }
                if (nextChunkX < chunkX || nextChunkY < chunkY || nextChunkZ < chunkZ) {
                    queue.clear();
                    return 0;
                }
                count++;
                currentGroup.add(pos);

                Direction dir = pair.getDirection();
                if (dir != Direction.EAST && nextX > 0) {
                    queue.add(new CarriedData(PosUtil.toInt((short) (nextX - 1), nextY, nextZ), Direction.WEST));
                }
                if (dir != Direction.UP && nextY > 0) {
                    queue.add(new CarriedData(PosUtil.toInt(nextX, (short) (nextY - 1), nextZ), Direction.DOWN));
                }
                if (dir != Direction.NORTH && nextZ > 0) {
                    queue.add(new CarriedData(PosUtil.toInt(nextX, nextY, (short) (nextZ - 1)), Direction.SOUTH));
                }
                if (dir != Direction.WEST && nextX < 254) {
                    queue.add(new CarriedData(PosUtil.toInt((short) (nextX + 1), nextY, nextZ), Direction.EAST));
                }
                if (dir != Direction.DOWN && nextY < 254) {
                    queue.add(new CarriedData(PosUtil.toInt(nextX, (short) (nextY + 1), nextZ), Direction.UP));
                }
                if (dir != Direction.SOUTH && nextZ < 254) {
                    queue.add(new CarriedData(PosUtil.toInt(nextX, nextY, (short) (nextZ + 1)), Direction.NORTH));
                }
            }
        }
        return count;
    }

    private void analyzeGroup() {
        AABB bounds = null;
        for (int pos : currentGroup) {
            short nextX = PosUtil.getX(pos);
            short nextY = PosUtil.getY(pos);
            short nextZ = PosUtil.getZ(pos);
            if (bounds == null) {
                bounds = new AABB(nextX, nextY, nextZ, nextX, nextY, nextZ);
            } else {
                bounds.include(nextX, nextY, nextZ);
            }
        }

        if (bounds != null) {
            int width = bounds.getWidth() + 1;
            int height = bounds.getHeight() + 1;
            int depth = bounds.getDepth() + 1;

            // Convert 3D plane to normalized 2D plane
            boolean[][] grid;
            if (width == height) {
                int minX = bounds.getMinX();
                int minY = bounds.getMinY();
                grid = new boolean[width][width];
                for (int pos : currentGroup) {
                    grid[PosUtil.getX(pos) - minX][PosUtil.getY(pos) - minY] = true;
                }
            } else if (width == depth) {
                int minX = bounds.getMinX();
                int minZ = bounds.getMinZ();
                grid = new boolean[width][width];
                for (int pos : currentGroup) {
                    grid[PosUtil.getX(pos) - minX][PosUtil.getZ(pos) - minZ] = true;
                }
            } else if (height == depth) {
                int minY = bounds.getMinY();
                int minZ = bounds.getMinZ();
                grid = new boolean[depth][depth];
                for (int pos : currentGroup) {
                    grid[PosUtil.getZ(pos) - minZ][PosUtil.getY(pos) - minY] = true;
                }
            } else {
                return;
            }
            if (Pattern2D.solveSwastika2D(grid, currentGroup.size())) {
                System.out.println("Found");
            }
        }
    }

    private void convertToNormalized2DPlane() {

    }

    private static class CarriedData {

        private final int key;
        private final Direction direction;

        public CarriedData(int key, Direction direction) {
            this.key = key;
            this.direction = direction;
        }

        public int getKey() {
            return key;
        }

        public Direction getDirection() {
            return direction;
        }
    }
}
