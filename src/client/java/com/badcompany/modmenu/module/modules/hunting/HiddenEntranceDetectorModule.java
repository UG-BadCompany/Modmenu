package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class HiddenEntranceDetectorModule extends AbstractBlockScanModule {
    private final BooleanSetting includeWaterElevators = addSetting(new BooleanSetting("Water elevators", "Flag suspicious vertical water columns as possible hidden entrances.", true));

    public HiddenEntranceDetectorModule() {
        super("Hidden Entrance Detector", "Looks for trapdoors, carpet coverings, buried ladders, water elevators, suspicious holes, and underground entrances.", 48.0D, 60.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (blockIdContains(blockId, "trapdoor", "carpet", "ladder", "obsidian") || (includeWaterElevators.get() && blockIdContains(blockId, "water"))) {
            if (suspicious(world, pos, blockId)) database.recordTrail(pos, dimension, "hidden_entrance:" + blockId);
        }
    }

    private boolean suspicious(ClientWorld world, BlockPos pos, String blockId) {
        if (blockIdContains(blockId, "ladder") && !world.getBlockState(pos.down()).isAir()) return true;
        if (blockIdContains(blockId, "carpet", "trapdoor")) return true;
        if (blockIdContains(blockId, "water") && blockIdContains(net.minecraft.registry.Registries.BLOCK.getId(world.getBlockState(pos.up()).getBlock()).toString(), "water")) return true;
        return blockIdContains(blockId, "obsidian") && world.getBlockState(pos.down()).isAir();
    }
}
