package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class TerrainAnomalyFinderModule extends AbstractBlockScanModule {
    private final NumberSetting anomalyThreshold = addSetting(new NumberSetting("Anomaly threshold", "Nearby matching terrain edits required before flagging an anomaly.", 24.0D, 8.0D, 128.0D));

    public TerrainAnomalyFinderModule() {
        super("Terrain Anomaly Finder", "Highlights large flat areas, TNT scars, excavated terrain, chunk-aligned holes, artificial walls, and large tunnels.", 64.0D, 100.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (blockIdContains(blockId, "obsidian", "cobblestone", "stone_bricks", "glass") && wallCount(world, pos, blockId) >= anomalyThreshold.get()) {
            database.recordTrail(pos, dimension, "terrain_anomaly:wall:" + blockId);
        }
        if (world.getBlockState(pos).isAir() && pos.getX() % 16 == 0 && pos.getZ() % 16 == 0) {
            database.recordTrail(pos, dimension, "terrain_anomaly:chunk_aligned_hole");
        }
    }

    private int wallCount(ClientWorld world, BlockPos pos, String blockId) {
        int count = 0;
        for (BlockPos scan : BlockPos.iterateOutwards(pos, 8, 8, 8)) {
            String id = net.minecraft.registry.Registries.BLOCK.getId(world.getBlockState(scan).getBlock()).toString();
            if (id.equals(blockId)) count++;
        }
        return count;
    }
}
