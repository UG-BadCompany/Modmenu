package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class BaseArchaeologyModule extends AbstractBlockScanModule {
    private final NumberSetting probabilityThreshold = addSetting(new NumberSetting("Probability threshold", "Minimum archaeology score before an ancient base clue is recorded.", 40.0D, 10.0D, 100.0D));
    public BaseArchaeologyModule() { super("Base Archaeology", "Detects old-base evidence such as random obsidian, dirt patches, TNT scars, casts, furnaces, crafting tables, leaf decay, and floating blocks.", 64.0D, 120.0D); }
    @Override protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        int score = 0;
        if (blockIdContains(blockId, "obsidian", "cobblestone", "stone_bricks")) score += 20;
        if (blockIdContains(blockId, "dirt", "grass_path", "path")) score += 10;
        if (blockIdContains(blockId, "furnace", "crafting_table")) score += 25;
        if (blockIdContains(blockId, "water", "lava")) score += 10;
        if (blockIdContains(blockId, "tnt")) score += 30;
        if (world.getBlockState(pos.down()).isAir() && !state.isAir()) score += 15;
        if (score >= probabilityThreshold.get()) database.recordBase(pos, dimension, Math.min(100, score), 768, "Ancient Base Probability: archaeology evidence " + blockId);
    }
}
