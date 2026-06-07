package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class PortalDatabaseModule extends AbstractBlockScanModule {
    private final BooleanSetting recordHighwayDistance = addSetting(new BooleanSetting("Highway distance", "Store approximate distance from nearest X/Z axis highway.", true));

    public PortalDatabaseModule() {
        super("Portal Database", "Automatically records every loaded nether portal with coordinates and timestamps.", 64.0D, 80.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (blockId.equals("minecraft:nether_portal")) database.recordPortal(pos, dimension, recordHighwayDistance.get() ? highwayDistance(pos) : -1.0D);
    }
}
