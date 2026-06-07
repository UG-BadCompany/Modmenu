package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public final class SafeLogoutFinderModule extends AbstractBlockScanModule {
    private final NumberSetting safeScore = addSetting(new NumberSetting("Safe score", "Minimum covered-air score before a logout spot is recorded.", 45.0D, 10.0D, 100.0D));
    public SafeLogoutFinderModule() { super("Safe Logout Finder", "Rates underground holes, hidden caves, and covered areas for safer logout positions.", 32.0D, 80.0D); }
    @Override protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (state.isAir()) return;
        BlockPos candidate = pos.up();
        if (!world.getBlockState(candidate).isAir()) return;
        int score = 10;
        if (!world.getBlockState(candidate.up()).isAir()) score += 20;
        if (!world.getBlockState(candidate.down()).isAir()) score += 10;
        if (!world.getBlockState(candidate.north()).isAir()) score += 10;
        if (!world.getBlockState(candidate.south()).isAir()) score += 10;
        if (!world.getBlockState(candidate.east()).isAir()) score += 10;
        if (!world.getBlockState(candidate.west()).isAir()) score += 10;
        if (candidate.getY() < 50) score += 15;
        if (score >= safeScore.get()) database.recordEvidence(candidate, dimension, "safe_logout", "Safety score " + score);
    }
}
