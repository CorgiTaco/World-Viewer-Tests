package dev.corgitaco;

import dev.corgitaco.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateShiftManagerTest {

    @Test
    public void testAll() {
        testConversions();
        testRegionTileInsertions();
        testMultiRegionSpiralFill();
    }

    @Test
    public void testConversions() {
        {
            CoordinateShiftManager manager = new CoordinateShiftManager(10, 1);

            assertEquals(1024, manager.getRegionImageSize());
            assertEquals(2048, manager.getRegionBlockSize());

            assertEquals(2048, manager.getBlockCoordFromRegionCoord(1));
            assertEquals(1, manager.getRegionCoordFromBlockCoord(2048));

            assertEquals(64, manager.getTileBlockSize());

            assertEquals(16, manager.getTileCoordFromBlockCoord(1024));
            assertEquals(1024, manager.getBlockCoordFromTileCoord(16));

            assertEquals(manager.getTileBlockSize() * 16, 1024);
        }

        {
            CoordinateShiftManager manager = new CoordinateShiftManager(10, 2);

            assertEquals(1024, manager.getRegionImageSize());
            assertEquals(4096, manager.getRegionBlockSize());

            assertEquals(4096, manager.getBlockCoordFromRegionCoord(1));
            assertEquals(1, manager.getRegionCoordFromBlockCoord(4096));

            assertEquals(128, manager.getTileBlockSize());

            assertEquals(8, manager.getTileCoordFromBlockCoord(1024));
            assertEquals(1024, manager.getBlockCoordFromTileCoord(8));

            assertEquals(manager.getTileBlockSize() * 8, 1024);
        }
    }

    @Test
    public void testRegionTileInsertions() {
        CoordinateShiftManager manager = new CoordinateShiftManager(10, 2);

        long regionPos = asLong(0, 0);

        TileRenderRegion tileRenderRegion = new TileRenderRegion(manager, regionPos);


        for (int x = 0; x < manager.getTileImageSize(); x++) {
            for (int z = 0; z < manager.getTileImageSize(); z++) {
                int minTileWorldX = manager.getBlockCoordFromTileCoord(x);
                int minTileWorldZ = manager.getBlockCoordFromTileCoord(z);
                assertTrue(tileRenderRegion.insertLayer(new SingleScreenTileLayer(minTileWorldX, minTileWorldZ), false));
            }
        }
        for (SingleScreenTileLayer layer : tileRenderRegion.getLayers()) {
            assertNotNull(layer);
        }

        for (int x = manager.getTileImageSize() + 1; x <= manager.getTileImageSize() + 1; x++) {
            for (int z = manager.getTileImageSize() + 1; z <= manager.getTileImageSize() + 1; z++) {
                int minTileWorldX = manager.getBlockCoordFromTileCoord(x);
                int minTileWorldZ = manager.getBlockCoordFromTileCoord(z);
                assertFalse(tileRenderRegion.insertLayer(new SingleScreenTileLayer(minTileWorldX, minTileWorldZ), false));
            }
        }

    }

    @Test
    public void testMultiRegionSpiralFill() {
        CoordinateShiftManager manager = new CoordinateShiftManager(10, 1);
        BlockPos origin = new BlockPos(2000000, 0, 290843);

        Map<Long, TileRenderRegion> regions = new HashMap<>();

        int slices = 360;
        double sliceSize = (Math.PI * 2D) / slices;

        int minX = origin.x() - 5000;
        int minZ = origin.z() - 5000;
        int maxX = origin.x() + 5000;
        int maxZ = origin.z() + 5000;
        RenderSquare worldViewArea = new RenderSquare(minX, minZ, maxX, maxZ);


        int xTileRange = manager.getTileCoordFromBlockCoord((maxX - minX) / 2);
        int zTileRange = manager.getTileCoordFromBlockCoord((maxZ - minZ) / 2);

        int tileRange = Math.max(xTileRange, zTileRange) + 2;

        for (int tileDistanceFromOrigin = 0; tileDistanceFromOrigin <= tileRange; tileDistanceFromOrigin++) {
            int tileSize = manager.getTileBlockSize();

            int originTileX = manager.getTileCoordFromBlockCoord(origin.x());
            int originTileZ = manager.getTileCoordFromBlockCoord(origin.z());

            int originWorldX = manager.getBlockCoordFromTileCoord(originTileX) + (tileSize / 2);
            int originWorldZ = manager.getBlockCoordFromTileCoord(originTileZ) + (tileSize / 2);

            double distance = tileSize * tileDistanceFromOrigin;

            for (int i = 0; i < slices; i++) {
                double angle = i * sliceSize;
                int worldTileX = (int) Math.round(originWorldX + (Math.sin(angle) * distance));
                int worldTileZ = (int) Math.round(originWorldZ + (Math.cos(angle) * distance));
                if (worldViewArea.intersects(worldTileX, worldTileZ, worldTileX, worldTileZ)) {

                    int tileXCoord = manager.getTileCoordFromBlockCoord(worldTileX);
                    int tileZCoord = manager.getTileCoordFromBlockCoord(worldTileZ);

                    int tileMinBlockX = manager.getBlockCoordFromTileCoord(tileXCoord);
                    int tileMinBlockZ = manager.getBlockCoordFromTileCoord(tileZCoord);
                    SingleScreenTileLayer singleScreenTileLayer = new SingleScreenTileLayer(tileMinBlockX, tileMinBlockZ);

                    int minTileWorldX = singleScreenTileLayer.minTileWorldX();
                    int minTileWorldZ = singleScreenTileLayer.minTileWorldZ();

                    int regionX = manager.getRegionCoordFromBlockCoord(minTileWorldX);
                    int regionZ = manager.getRegionCoordFromBlockCoord(minTileWorldZ);

                    TileRenderRegion tileRenderRegion = regions.computeIfAbsent(asLong(regionX, regionZ), regionKey -> new TileRenderRegion(manager, regionKey));


                    assertTrue(tileRenderRegion.insertLayer(singleScreenTileLayer));
                }
            }
        }

        for (Map.Entry<Long, TileRenderRegion> longTileRenderRegionEntry : regions.entrySet()) {
            TileRenderRegion value = longTileRenderRegionEntry.getValue();
            assertTrue((value.intersects(worldViewArea)));
        }
    }


    public static long asLong(int $$0, int $$1) {
        return (long) $$0 & 4294967295L | ((long) $$1 & 4294967295L) << 32;
    }
}
