package ca.fxco.swastikafinder.util;

public class AABB {

    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    public AABB() {}

    public AABB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMinZ() {
        return minZ;
    }
    public void setMinZ(int minZ) {
        this.minZ = minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

    // Calculate width (size in X axis)
    public int getWidth() {
        return maxX - minX;
    }

    // Calculate height (size in Y axis)
    public int getHeight() {
        return maxY - minY;
    }

    // Calculate depth (size in Z axis)
    public int getDepth() {
        return maxZ - minZ;
    }

    // Calculate the diagonal length of the AABB
    public double getDiagonalLength() {
        int dx = getWidth();
        int dy = getHeight();
        int dz = getDepth();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // Includes this position within the bounds
    public void include(int x, int y, int z) {
        this.minX = Math.min(this.minX, x);
        this.minY = Math.min(this.minY, y);
        this.minZ = Math.min(this.minZ, z);
        this.maxX = Math.max(this.maxX, x);
        this.maxY = Math.max(this.maxY, y);
        this.maxZ = Math.max(this.maxZ, z);
    }

    // Check if this AABB intersects with another AABB
    public boolean intersects(AABB other) {
        return this.maxX > other.minX && this.minX < other.maxX &&
                this.maxY > other.minY && this.minY < other.maxY &&
                this.maxZ > other.minZ && this.minZ < other.maxZ;
    }

    // Check if this AABB contains a point
    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    // Check if this AABB contains another AABB
    public boolean contains(AABB other) {
        return this.minX <= other.minX && this.maxX >= other.maxX &&
                this.minY <= other.minY && this.maxY >= other.maxY &&
                this.minZ <= other.minZ && this.maxZ >= other.maxZ;
    }

    // Expand this AABB by a certain amount
    public AABB expand(int dx, int dy, int dz) {
        return new AABB(
                minX - dx, minY - dy, minZ - dz,
                maxX + dx, maxY + dy, maxZ + dz
        );
    }

    // Contract this AABB by a certain amount
    public AABB contract(int dx, int dy, int dz) {
        return new AABB(
                minX + dx, minY + dy, minZ + dz,
                maxX - dx, maxY - dy, maxZ - dz
        );
    }

    // Get the volume of this AABB
    public int getVolume() {
        return getWidth() * getHeight() * getDepth();
    }

    // Get the center of the AABB
    public int[] getCenter() {
        return new int[] {
                (minX + maxX) / 2,
                (minY + maxY) / 2,
                (minZ + maxZ) / 2
        };
    }

    // Get the center of the AABB
    public float getCenterX() {
        return getWidth() / 2F;
    }

    // Get the center of the AABB
    public float getCenterY() {
        return getHeight() / 2F;
    }

    // Get the center of the AABB
    public float getCenterZ() {
        return getDepth() / 2F;
    }

    // Translate (move) this AABB by an offset
    public AABB translate(int dx, int dy, int dz) {
        return new AABB(
                minX + dx, minY + dy, minZ + dz,
                maxX + dx, maxY + dy, maxZ + dz
        );
    }

    // Calculate the surface area of this AABB
    public int getSurfaceArea() {
        int dx = maxX - minX;
        int dy = maxY - minY;
        int dz = maxZ - minZ;
        return 2 * (dx * dy + dy * dz + dz * dx);
    }

    // Check if this AABB is valid (min is less than or equal to max)
    public boolean isValid() {
        return minX <= maxX && minY <= maxY && minZ <= maxZ;
    }

    @Override
    public String toString() {
        return "AABB[minX=" + minX + ", minY=" + minY + ", minZ=" + minZ +
                ", maxX=" + maxX + ", maxY=" + maxY + ", maxZ=" + maxZ + "]";
    }

    // Check equality of two AABBs
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AABB)) return false;
        AABB other = (AABB) obj;
        return this.minX == other.minX && this.minY == other.minY && this.minZ == other.minZ &&
                this.maxX == other.maxX && this.maxY == other.maxY && this.maxZ == other.maxZ;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(minX);
        result = 31 * result + Integer.hashCode(minY);
        result = 31 * result + Integer.hashCode(minZ);
        result = 31 * result + Integer.hashCode(maxX);
        result = 31 * result + Integer.hashCode(maxY);
        result = 31 * result + Integer.hashCode(maxZ);
        return result;
    }
}
