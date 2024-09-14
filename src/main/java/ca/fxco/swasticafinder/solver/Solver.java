package ca.fxco.swasticafinder.solver;

import ca.fxco.swasticafinder.chunk.Region;
import ca.fxco.swasticafinder.chunk.SubChunk;
import ca.fxco.swasticafinder.util.AABB;
import ca.fxco.swasticafinder.util.PosUtil;
import it.unimi.dsi.fastutil.ints.*;

import java.util.ArrayDeque;
import java.util.Queue;

public class Solver {

    private final Queue<CarriedData> queue = new ArrayDeque<>();
    private final IntSet positions = new IntOpenHashSet(40);
    private final IntSet currentGroup = new IntOpenHashSet(18);

    public int solve(Region region, byte[] blockIds) {
        int countSearchedChunks = 0;
        for (byte x = 0; x < 16; x++) {
            short startX = (short) (x << 4);
            for (byte y = 0; y < 16; y++) {
                short startY = (short) (y << 4);
                for (byte z = 0; z < 16; z++) {
                    short startZ = (short) (z << 4);
                    SubChunk chunk = region.getChunk(x, y, z);
                    for (byte blockId : blockIds) {
                        if (chunk.doesPaletteContain(blockId)) {
                            countSearchedChunks++;
                            solveChunk(region, startX, startY, startZ, blockId);
                        }
                    }
                }
            }
        }
        return countSearchedChunks;
    }

    public void solveChunk(Region region, short startX, short startY, short startZ, byte blockId) {
        positions.clear(); // TODO: Remove this for faster cross-chunk scans
        int chunkMaxX = startX + 16;
        int chunkMaxY = startY + 16;
        int chunkMaxZ = startZ + 16;
        for (short x = startX; x < chunkMaxX; x++) {
            for (short y = startY; y < chunkMaxY; y++) {
                for (short z = startZ; z < chunkMaxZ; z++) {
                    if (optimizedBFT(region, x, y, z, blockId) > 12) {
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
                if (nextChunkX < chunkX || nextChunkY < chunkY || nextChunkZ < chunkZ) {
                    queue.clear();
                    return 0;
                }
                SubChunk nextChunk = region.getChunk(nextChunkX, nextChunkY, nextChunkZ);
                if (nextChunk.getBlock((byte) (nextX & 15), (byte) (nextY & 15), (byte) (nextZ & 15)) != blockId) {
                    continue;
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
            int width = bounds.getWidth();
            int height = bounds.getHeight();
            int depth = bounds.getDepth();

            // 1. Ensure two sides are the same size
            Axis ignoreAxis;
            if (width == height) {
                ignoreAxis = Axis.Z;
            } else if (width == depth) {
                ignoreAxis = Axis.Y;
            } else if (height == depth) {
                ignoreAxis = Axis.X;
            } else {
                return;
            }
            // 2. Check if there's a cross in the center
            int centerX = bounds.getMinX() + width / 2;
            int centerY = bounds.getMinY() + height / 2;
            int centerZ = bounds.getMinZ() + depth / 2;
            boolean crossExists = checkCross(centerX, centerY, centerZ, ignoreAxis);
            if (crossExists) {
                // 3. Check that each arm has a right angle at the end (going right)
                boolean rightAngleAtEnds = checkRightAnglesAtEnds(centerX, centerY, centerZ, width, height, depth, ignoreAxis);
                if (rightAngleAtEnds) {
                    // TODO 4. Ensure all sides are the same size
                    System.out.println("Found a valid pattern!");
                }
            }
        }
    }

    // Helper method to check if a cross exists at the center
    private boolean checkCross(int centerX, int centerY, int centerZ, Axis ignore) {
        if (ignore == Axis.Z) {
            return currentGroup.contains(PosUtil.toInt((short) (centerX - 1), (short) centerY, (short) centerZ)) &&
                    currentGroup.contains(PosUtil.toInt((short) (centerX + 1), (short) centerY, (short) centerZ)) &&
                    currentGroup.contains(PosUtil.toInt((short) centerX, (short) (centerY - 1), (short) centerZ)) &&
                    currentGroup.contains(PosUtil.toInt((short) centerX, (short) (centerY + 1), (short) centerZ));
        } else if (ignore == Axis.X) {
            return currentGroup.contains(PosUtil.toInt((short) centerX, (short) (centerY - 1), (short) centerZ)) &&
                    currentGroup.contains(PosUtil.toInt((short) centerX, (short) (centerY + 1), (short) centerZ)) &&
                    currentGroup.contains(PosUtil.toInt((short) centerX, (short) centerY, (short) (centerZ - 1))) &&
                    currentGroup.contains(PosUtil.toInt((short) centerX, (short) centerY, (short) (centerZ + 1)));
        } else if (ignore == Axis.Y) {
            return currentGroup.contains(PosUtil.toInt((short) (centerX - 1), (short) centerY, (short) centerZ)) &&
                    currentGroup.contains(PosUtil.toInt((short) (centerX + 1), (short) centerY, (short) centerZ)) &&
                    currentGroup.contains(PosUtil.toInt((short) centerX, (short) centerY, (short) (centerZ - 1))) &&
                    currentGroup.contains(PosUtil.toInt((short) centerX, (short) centerY, (short) (centerZ + 1)));
        }
        return false;
    }

    // Helper method to check right angles at the ends of the cross arms
    private boolean checkRightAnglesAtEnds(int centerX, int centerY, int centerZ, int width, int height, int depth,
                                           Axis ignoreAxis) {
        // Check at each end of the cross for a right angle block going "right"
        boolean valid = true;

        if (ignoreAxis == Axis.X) {
            // The cross is along the Y and Z axes
            // Check on Y axis ends - right angle should go along the Z axis
            valid &= checkRightAngle(centerX, centerY - height / 2, centerZ, Axis.Z);
            valid &= checkRightAngle(centerX, centerY + height / 2, centerZ, Axis.Z);

            // Check on Z axis ends - right angle should go along the Y axis
            valid &= checkRightAngle(centerX, centerY, centerZ - depth / 2, Axis.Y);
            valid &= checkRightAngle(centerX, centerY, centerZ + depth / 2, Axis.Y);
        } else if (ignoreAxis == Axis.Y) {
            // The cross is along the X and Z axes
            // Check on X axis ends - right angle should go along the Z axis
            valid &= checkRightAngle(centerX - width / 2, centerY, centerZ, Axis.Z);
            valid &= checkRightAngle(centerX + width / 2, centerY, centerZ, Axis.Z);

            // Check on Z axis ends - right angle should go along the X axis
            valid &= checkRightAngle(centerX, centerY, centerZ - depth / 2, Axis.X);
            valid &= checkRightAngle(centerX, centerY, centerZ + depth / 2, Axis.X);
        } else if (ignoreAxis == Axis.Z) {
            // The cross is along the X and Y axes
            // Check on X axis ends - right angle should go along the Y axis
            valid &= checkRightAngle(centerX - width / 2, centerY, centerZ, Axis.Y);
            valid &= checkRightAngle(centerX + width / 2, centerY, centerZ, Axis.Y);

            // Check on Y axis ends - right angle should go along the X axis
            valid &= checkRightAngle(centerX, centerY - height / 2, centerZ, Axis.X);
            valid &= checkRightAngle(centerX, centerY + height / 2, centerZ, Axis.X);
        }

        return valid;
    }

    // Helper method to check if there's a right angle at a specific position, based on the axis
    private boolean checkRightAngle(int x, int y, int z, Axis axis) {
        return switch (axis) {
            case X ->
                // Right angle along the X-axis means we check the block to the right (+X) of the current position
                    currentGroup.contains(PosUtil.toInt((short) (x + 1), (short) y, (short) z)) || currentGroup.contains(PosUtil.toInt((short) (x - 1), (short) y, (short) z));
            case Y ->
                // Right angle along the Y-axis means we check the block above (+Y) or below (-Y)
                    currentGroup.contains(PosUtil.toInt((short) x, (short) (y + 1), (short) z)) || currentGroup.contains(PosUtil.toInt((short) x, (short) (y - 1), (short) z));
            case Z ->
                // Right angle along the Z-axis means we check the block in front (+Z) or behind (-Z)
                    currentGroup.contains(PosUtil.toInt((short) x, (short) y, (short) (z + 1))) || currentGroup.contains(PosUtil.toInt((short) x, (short) y, (short) (z - 1)));
        };
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

    private enum Direction {
        NORTH, EAST, SOUTH, WEST, UP, DOWN
    }

    private enum Axis {
        X, Y, Z
    }
}
