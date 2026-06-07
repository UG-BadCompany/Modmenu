package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class ArtificialBlockDetectorModule extends AbstractBlockScanModule {
    private final BooleanSetting highlightSuspicious = addSetting(new BooleanSetting("Highlight suspicious", "Expose suspicious artificial blocks for ESP/highlight rendering.", true));

    public ArtificialBlockDetectorModule() {
        super("Artificial Block Detector", "Records suspicious non-natural blocks such as floating dirt, pillars, glass, farmland, torches, and utility blocks.", 48.0D, 50.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (blockIdContains(blockId, "glass", "farmland", "dirt_path", "stripped_", "torch", "crafting_table", "furnace") || isFloatingDirt(world, pos, blockId)) {
            database.recordTrail(pos, dimension, "suspicious:" + blockId);
        }
    }

    public boolean highlightSuspicious() { return highlightSuspicious.get(); }

    private boolean isFloatingDirt(ClientWorld world, BlockPos pos, String blockId) {
        return blockId.contains("dirt") && world.getBlockState(pos.down()).isAir() && world.getBlockState(pos.up()).isAir();
    }
}
