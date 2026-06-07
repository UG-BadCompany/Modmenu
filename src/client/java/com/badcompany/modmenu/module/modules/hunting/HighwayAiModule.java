package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class HighwayAiModule extends AbstractBlockScanModule {
    private final NumberSetting branchDistance = addSetting(new NumberSetting("Branch distance", "Minimum distance from axis/ring before a side branch is marked.", 32.0D, 4.0D, 256.0D));
    public HighwayAiModule() { super("Highway AI", "Maps cardinal, diagonal, ring, ice, boat-road, and side-tunnel highway evidence and branches.", 64.0D, 80.0D); }
    @Override protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        boolean road = blockIdContains(blockId, "obsidian", "packed_ice", "blue_ice", "rail", "netherrack", "stone");
        if (!road) return;
        boolean cardinal = Math.abs(pos.getX()) <= 4 || Math.abs(pos.getZ()) <= 4;
        boolean diagonal = Math.abs(Math.abs(pos.getX()) - Math.abs(pos.getZ())) <= 4;
        boolean ring = Math.abs(Math.hypot(pos.getX(), pos.getZ()) % 10000.0D) <= 8.0D;
        if (cardinal || diagonal || ring) database.recordTrail(pos, dimension, "highway_ai:" + blockId);
        else if (highwayDistance(pos) > branchDistance.get()) database.recordExpedition(pos, dimension, "Investigate possible highway branch or side tunnel", false);
    }
}
