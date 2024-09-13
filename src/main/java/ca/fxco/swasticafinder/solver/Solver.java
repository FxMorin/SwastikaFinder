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
                            solveChunk(region, chunk, startX, startY, startZ, blockId);
                        }
                    }
                }
            }
        }
        return countSearchedChunks;
    }

    public void solveChunk(Region region, SubChunk chunk, short startX, short startY, short startZ, byte blockId) {
        positions.clear(); // TODO: Remove this for faster cross-chunk scans
        for (short x = startX; x < startX + 16; x++) {
            for (short y = startY; y < startY + 16; y++) {
                for (short z = startZ; z < startZ + 16; z++) {
                    int pos = PosUtil.toInt(x, y, z);
                    if (positions.contains(pos)) {
                        continue;
                    }
                    currentGroup.clear();
                    int size = optimizedBFT(positions, pos, s -> region.getBlock(s) == blockId, (a, c, d) -> {
                        short nextX = PosUtil.getX(a);
                        short nextY = PosUtil.getY(a);
                        short nextZ = PosUtil.getZ(a);
                        currentGroup.add(a);

                        if (nextX > 0 && d != Direction.EAST) {
                            c.accept(PosUtil.toInt((short) (nextX - 1), nextY, nextZ), Direction.WEST);
                        }
                        if (nextY > 0 && d != Direction.UP) {
                            c.accept(PosUtil.toInt(nextX, (short) (nextY - 1), nextZ), Direction.DOWN);
                        }
                        if (nextZ > 0 && d != Direction.NORTH) {
                            c.accept(PosUtil.toInt(nextX, nextY, (short) (nextZ - 1)), Direction.SOUTH);
                        }
                        if (nextX < 254 && d != Direction.WEST) {
                            c.accept(PosUtil.toInt((short) (nextX + 1), nextY, nextZ), Direction.EAST);
                        }
                        if (nextY < 254 && d != Direction.DOWN) {
                            c.accept(PosUtil.toInt(nextX, (short) (nextY + 1), nextZ), Direction.UP);
                        }
                        if (nextZ < 254 && d != Direction.SOUTH) {
                            c.accept(PosUtil.toInt(nextX, nextY, (short) (nextZ + 1)), Direction.NORTH);
                        }
                    });
                    if (size > 0) {
                        analyzeGroup();
                    }
                }
            }
        }
    }

    private int optimizedBFT(IntSet positions, int startPos, IntPredicate isValid, ActionConsumer action) {
        queue.clear();
        queue.add(CarriedData.of(startPos, (short) 0, null));
        int count = 0;

        while(!queue.isEmpty()) {
            CarriedData pair = queue.poll();
            int pos = pair.getKey();
            if (positions.add(pos)) {
                if (isValid.test(pos)) {
                    count++;
                    action.accept(
                            pos,
                            (newPos, dir) -> queue.add(CarriedData.of(newPos, (short) (pair.getValue() + 1), dir)),
                            pair.getDirection()
                    );
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

            //System.out.println("width: " + width + ", height: " + height + ", depth: " + depth);
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
        private final short value;
        private final Direction direction;

        public CarriedData(int key, short value, Direction direction) {
            this.key = key;
            this.value = value;
            this.direction = direction;
        }

        public int getKey() {
            return key;
        }

        public short getValue() {
            return value;
        }

        public Direction getDirection() {
            return direction;
        }

        public static CarriedData of(int key, short value, Direction direction) {
            return new CarriedData(key, value, direction);
        }
    }

    @FunctionalInterface
    private interface ActionConsumer {

        void accept(int i, IntDirectionConsumer intConsumer, Direction from);
    }

    @FunctionalInterface
    private interface IntDirectionConsumer {

        void accept(int i, Direction from);
    }

    private enum Direction {
        NORTH, EAST, SOUTH, WEST, UP, DOWN
    }

    private enum Axis {
        X, Y, Z
    }
}
