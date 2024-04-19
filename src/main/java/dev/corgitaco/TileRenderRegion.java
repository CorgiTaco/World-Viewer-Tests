package dev.corgitaco;

public class TileRenderRegion implements AutoCloseable {


    private final CoordinateShiftManager coordinateShiftManager;
    private final long regionPos;

    private final SingleScreenTileLayer[] layers;

    public Texture texture;


    public TileRenderRegion(CoordinateShiftManager coordinateShiftManager, long regionPos) {
        this.coordinateShiftManager = coordinateShiftManager;
        this.regionPos = regionPos;
        if (coordinateShiftManager.shift() % 2 != 0) {
            throw new IllegalArgumentException("Shift must be an even value");
        }

        int tileSize = coordinateShiftManager.getTileImageSize();
        layers = new SingleScreenTileLayer[(int) (tileSize * tileSize)];

        texture = new Texture(coordinateShiftManager.getRegionImageSize(), coordinateShiftManager.getRegionImageSize());
    }


    public boolean insertLayer(SingleScreenTileLayer layer) {
        return insertLayer(layer, true);
    }

    public boolean insertLayer(SingleScreenTileLayer layer, boolean duplicatesAllowed) {
        int regionWorldX = getRegionBlockX();
        int regionWorldZ = getRegionBlockZ();
        int minTileWorldX = layer.minTileWorldX();
        int minTileWorldZ = layer.minTileWorldZ();

        int localBlockX = minTileWorldX - regionWorldX;
        int localBlockZ = minTileWorldZ - regionWorldZ;

        int localTileXIdx = this.coordinateShiftManager.getTileCoordFromBlockCoord(localBlockX);
        int localTileZIdx = this.coordinateShiftManager.getTileCoordFromBlockCoord(localBlockZ);

        int tileImageSize = this.coordinateShiftManager.getTileImageSize();
        int idx = localTileXIdx + localTileZIdx * tileImageSize;

        if (idx > this.layers.length) {
            return false;
        }

        if (layers[idx] != null && !duplicatesAllowed) {
            return false;
        }

        layers[idx] = layer;

        return localTileXIdx * this.coordinateShiftManager.getTileImageSize() >= 0 && localTileZIdx * this.coordinateShiftManager.getTileImageSize() >= 0 && localTileXIdx * this.coordinateShiftManager.getTileImageSize() <= this.coordinateShiftManager.getRegionImageSize() && localTileZIdx * this.coordinateShiftManager.getTileImageSize() <= this.coordinateShiftManager.getRegionImageSize();
    }

    public SingleScreenTileLayer[] getLayers() {
        return layers;
    }

    public boolean intersects(RenderSquare square) {
        return square.intersectsRegion(getRegionBlockX(), getRegionBlockZ(), getRegionBlockX() + this.coordinateShiftManager.getRegionBlockSize(), getRegionBlockZ() + this.coordinateShiftManager.getRegionBlockSize());
    }


    public int getRegionBlockX() {
        return this.coordinateShiftManager.getRegionWorldX(regionPos);
    }

    public int getRegionBlockZ() {
        return this.coordinateShiftManager.getRegionWorldZ(regionPos);
    }

    public int getRegionX() {
        return this.coordinateShiftManager.getRegionX(regionPos);
    }

    public int getRegionZ() {
        return this.coordinateShiftManager.getRegionZ(regionPos);
    }

    @Override
    public void close() {
    }
}
