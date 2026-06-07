package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

abstract class AbstractBlockScanModule extends Module {
    protected final HuntDatabase database = HuntDatabase.get();
    private static final int DEFAULT_SCAN_BUDGET = 2048;
    private final NumberSetting range;
    private final NumberSetting intervalTicks;
    private final NumberSetting scanBudget;
    private final List<BlockPos> offsets = new ArrayList<>();
    private int lastRange = -1;
    private int cursor;
    private int cooldown;

    protected AbstractBlockScanModule(String name, String description, double defaultRange, double defaultInterval) {
        super(name, description, Category.HUNTING);
        this.range = addSetting(new NumberSetting("Scan range", "Nearby loaded-block scan radius. Kept bounded to prevent FPS drops.", defaultRange, 8.0D, 96.0D));
        this.intervalTicks = addSetting(new NumberSetting("Scan interval", "Ticks between incremental scan passes.", defaultInterval, 1.0D, 200.0D));
        this.scanBudget = addSetting(new NumberSetting("Scan budget", "Maximum blocks processed per tick.", DEFAULT_SCAN_BUDGET, 128.0D, 8192.0D));
    }

    @Override
    protected void onEnable() {
        rebuildOffsets(true);
        cursor = 0;
        cooldown = 0;
    }

    @Override
    protected void onDisable() {
        cursor = 0;
        offsets.clear();
    }

    @Override
    public void tick() {
        database.tickAutosave();
        if (client.world == null || client.player == null) return;
        rebuildOffsets(false);
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        scan(client.world, client.player.getBlockPos());
    }

    protected void onScanComplete() {}
    protected abstract void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension);

    protected boolean blockIdContains(String blockId, String... needles) {
        String lower = blockId.toLowerCase(Locale.ROOT);
        for (String needle : needles) if (lower.contains(needle)) return true;
        return false;
    }

    protected double highwayDistance(BlockPos pos) {
        int absX = Math.abs(pos.getX());
        int absZ = Math.abs(pos.getZ());
        return Math.min(absX, absZ);
    }

    private void scan(ClientWorld world, BlockPos origin) {
        if (offsets.isEmpty()) return;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int processed = 0;
        int budget = Math.max(128, (int) Math.round(scanBudget.get()));
        String dimension = HuntDatabase.dimension();
        while (processed < budget && cursor < offsets.size()) {
            BlockPos offset = offsets.get(cursor++);
            mutable.set(origin.getX() + offset.getX(), origin.getY() + offset.getY(), origin.getZ() + offset.getZ());
            if (world.isInBuildLimit(mutable)) {
                BlockState state = world.getBlockState(mutable);
                if (!state.isAir()) visit(world, mutable.toImmutable(), state, Registries.BLOCK.getId(state.getBlock()).toString(), dimension);
            }
            processed++;
        }
        if (cursor >= offsets.size()) {
            cursor = 0;
            cooldown = (int) Math.round(intervalTicks.get());
            onScanComplete();
        }
    }

    private void rebuildOffsets(boolean force) {
        int radius = (int) Math.round(range.get());
        if (!force && radius == lastRange) return;
        lastRange = radius;
        offsets.clear();
        int square = radius * radius;
        for (int y = -Math.min(radius, 32); y <= Math.min(radius, 32); y++) {
            for (int z = -radius; z <= radius; z++) {
                for (int x = -radius; x <= radius; x++) {
                    int dist = x * x + y * y + z * z;
                    if (dist <= square) offsets.add(new BlockPos(x, y, z));
                }
            }
        }
        offsets.sort((a, b) -> Integer.compare(a.getManhattanDistance(BlockPos.ORIGIN), b.getManhattanDistance(BlockPos.ORIGIN)));
        cursor = 0;
    }
}
