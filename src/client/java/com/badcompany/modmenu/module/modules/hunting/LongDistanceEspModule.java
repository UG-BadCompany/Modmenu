package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class LongDistanceEspModule extends AbstractBlockScanModule {
    private final NumberSetting portalDistance = addSetting(new NumberSetting("Portal distance", "Portal ESP collection radius.", 96.0D, 16.0D, 512.0D));
    private final NumberSetting storageDistance = addSetting(new NumberSetting("Storage distance", "Chest/shulker/hopper ESP collection radius.", 96.0D, 16.0D, 512.0D));
    private final BooleanSetting includeSigns = addSetting(new BooleanSetting("Include signs", "Include signs in long-distance ESP records.", true));

    public LongDistanceEspModule() {
        super("Long Distance ESP", "Collects long-distance ESP targets for portals, chests, ender chests, shulkers, hoppers, and signs.", 96.0D, 80.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (client.player == null) return;
        double dist = Math.sqrt(client.player.getBlockPos().getSquaredDistance(pos));
        if (blockId.equals("minecraft:nether_portal") && dist <= portalDistance.get()) database.recordPortal(pos, dimension, highwayDistance(pos));
        if (blockIdContains(blockId, "chest", "shulker_box", "hopper") && dist <= storageDistance.get()) database.recordStash(pos, dimension, 20, "ESP Storage");
        if (includeSigns.get() && blockIdContains(blockId, "sign")) database.recordSign(pos, dimension, "<text pending block-entity load>");
    }
}
