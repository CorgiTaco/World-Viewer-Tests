package dev.corgitaco;

public record RenderSquare(int minX, int minZ, int maxX, int maxZ) {


    public boolean intersects(int x0, int z0, int x1, int z1) {
        return this.maxX >= x0 && this.minX <= x1 && this.maxZ >= z0 && this.minZ <= z1;

    }

    public boolean intersectsRegion(int x0, int z0, int x1, int z1) {
        return this.maxX >= x0 && this.minX <= x1 && this.maxZ >= z0 && this.minZ <= z1;
    }
}
