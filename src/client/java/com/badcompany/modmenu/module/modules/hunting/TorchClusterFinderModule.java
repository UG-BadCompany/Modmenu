package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class TorchClusterFinderModule extends AbstractBlockScanModule {
    private final NumberSetting clusterSize = addSetting(new NumberSetting("Cluster size", "Minimum nearby torches before a possible camp is recorded.", 3.0D, 2.0D, 12.0D));

    public TorchClusterFinderModule() {
        super("Torch Cluster Finder", "Ignores isolated torches and flags clustered torches as possible camps.", 48.0D, 60.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (!blockIdContains(blockId, "torch")) return;
        int torches = 0;
        for (BlockPos scan : BlockPos.iterateOutwards(pos, 8, 4, 8)) {
            String id = net.minecraft.registry.Registries.BLOCK.getId(world.getBlockState(scan).getBlock()).toString();
            if (blockIdContains(id, "torch")) torches++;
            if (torches >= clusterSize.get()) {
                database.recordStash(pos, dimension, 35 + torches * 5, "Possible Camp");
                return;
            }
        }
    }
}
