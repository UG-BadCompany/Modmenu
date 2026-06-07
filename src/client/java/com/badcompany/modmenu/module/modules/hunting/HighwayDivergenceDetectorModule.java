package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class HighwayDivergenceDetectorModule extends AbstractBlockScanModule {
    private final NumberSetting highwayBand = addSetting(new NumberSetting("Highway band", "Distance from X/Z axis considered a highway corridor.", 12.0D, 2.0D, 64.0D));

    public HighwayDivergenceDetectorModule() {
        super("Highway Divergence Detector", "Detects side tunnels, obsidian paths, boat trails, ice paths, diagonal paths, and small tunnel branches leaving highways.", 64.0D, 60.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (Math.min(Math.abs(pos.getX()), Math.abs(pos.getZ())) > highwayBand.get()) return;
        if (blockIdContains(blockId, "obsidian", "ice", "blue_ice", "packed_ice", "rail") || tunnelLike(world, pos)) {
            database.recordTrail(pos, dimension, "highway_divergence:" + blockId);
        }
    }

    private boolean tunnelLike(ClientWorld world, BlockPos pos) {
        return world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir() && !world.getBlockState(pos.down()).isAir();
    }
}
