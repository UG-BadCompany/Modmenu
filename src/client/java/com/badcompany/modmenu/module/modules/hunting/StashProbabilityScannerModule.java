package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class StashProbabilityScannerModule extends AbstractBlockScanModule {
    private final BooleanSetting showConfidence = addSetting(new BooleanSetting("Show confidence", "Expose Possible Camp/Possible Stash/Likely Base confidence labels.", true));
    private final BooleanSetting recordPossibleCamps = addSetting(new BooleanSetting("Record camps", "Persist low-confidence camp/storage clusters instead of only likely bases.", true));

    public StashProbabilityScannerModule() {
        super("Stash Probability Scanner", "Scores storage and utility clusters as Possible Camp, Possible Stash, or Likely Base.", 64.0D, 60.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        int score = score(blockId);
        if (score <= 0) return;
        int nearby = nearbyStorage(world, pos);
        if (nearby >= 3) score += 50;
        String label = score >= 80 ? "Likely Base" : score >= 45 ? "Possible Stash" : "Possible Camp";
        if (score >= 45 || recordPossibleCamps.get()) database.recordStash(pos, dimension, score, label);
    }

    public boolean showConfidence() { return showConfidence.get(); }

    private int nearbyStorage(ClientWorld world, BlockPos pos) {
        int found = 0;
        for (BlockPos scan : BlockPos.iterateOutwards(pos, 6, 4, 6)) {
            String id = net.minecraft.registry.Registries.BLOCK.getId(world.getBlockState(scan).getBlock()).toString();
            if (score(id) > 0) found++;
            if (found >= 3) return found;
        }
        return found;
    }

    private int score(String blockId) {
        if (blockIdContains(blockId, "shulker_box")) return 30;
        if (blockIdContains(blockId, "hopper", "dispenser")) return 25;
        if (blockIdContains(blockId, "chest")) return 20;
        if (blockIdContains(blockId, "furnace", "crafting_table")) return 10;
        return 0;
    }
}
