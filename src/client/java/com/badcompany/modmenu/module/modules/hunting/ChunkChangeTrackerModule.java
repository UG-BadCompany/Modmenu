package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

public final class ChunkChangeTrackerModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting chunksPerTick = addSetting(new NumberSetting("Chunks per tick", "Visited chunks snapshotted per tick.", 1.0D, 1.0D, 8.0D));
    private final BooleanSetting highlightChanged = addSetting(new BooleanSetting("Highlight changed", "Expose changed chunks for rendering.", true));
    private final Set<String> visitedThisSession = new HashSet<>();
    private int spiralIndex;

    public ChunkChangeTrackerModule() {
        super("Chunk Change Tracker", "Snapshots visited chunks and records changed chunks, new portals, storage, signs, shulkers, and obsidian on revisit.", Category.HUNTING);
    }

    @Override
    protected void onDisable() { visitedThisSession.clear(); }

    @Override
    public void tick() {
        database.tickAutosave();
        if (client.world == null || client.player == null) return;
        String dimension = HuntDatabase.dimension();
        ChunkPos center = client.player.getChunkPos();
        int count = (int) Math.round(chunksPerTick.get());
        for (int i = 0; i < count; i++) {
            int dx = (spiralIndex % 5) - 2;
            int dz = ((spiralIndex / 5) % 5) - 2;
            spiralIndex = (spiralIndex + 1) % 25;
            snapshot(dimension, center.x + dx, center.z + dz);
        }
    }

    public boolean highlightChanged() { return highlightChanged.get(); }

    private void snapshot(String dimension, int chunkX, int chunkZ) {
        String key = dimension + ':' + chunkX + ':' + chunkZ;
        String hash = hashChunk(chunkX, chunkZ);
        String previous = database.chunkHash(dimension, chunkX, chunkZ);
        if (previous != null && !previous.equals(hash) && visitedThisSession.add(key + ':' + hash)) {
            BlockPos pos = new BlockPos(chunkX << 4, client.player.getBlockY(), chunkZ << 4);
            database.recordChunkChange(dimension, chunkX, chunkZ, "chunk_hash_changed", pos, hash);
            detectImportantBlocks(dimension, chunkX, chunkZ);
        }
        database.recordChunkSnapshot(dimension, chunkX, chunkZ, hash);
    }

    private String hashChunk(int chunkX, int chunkZ) {
        int hash = 1;
        int minY = Math.max(client.world.getBottomY(), client.player.getBlockY() - 16);
        int maxY = Math.min(client.world.getTopYInclusive(), client.player.getBlockY() + 16);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = minY; y <= maxY; y += 2) {
            pos.set((chunkX << 4) + x, y, (chunkZ << 4) + z);
            hash = 31 * hash + Registries.BLOCK.getRawId(client.world.getBlockState(pos).getBlock());
        }
        return Integer.toHexString(hash);
    }

    private void detectImportantBlocks(String dimension, int chunkX, int chunkZ) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        int minY = Math.max(client.world.getBottomY(), client.player.getBlockY() - 24);
        int maxY = Math.min(client.world.getTopYInclusive(), client.player.getBlockY() + 24);
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = minY; y <= maxY; y++) {
            pos.set((chunkX << 4) + x, y, (chunkZ << 4) + z);
            BlockState state = client.world.getBlockState(pos);
            String id = Registries.BLOCK.getId(state.getBlock()).toString();
            if (id.contains("portal") || id.contains("chest") || id.contains("shulker") || id.contains("sign") || id.contains("obsidian")) {
                database.recordChunkChange(dimension, chunkX, chunkZ, "important_block_seen", pos.toImmutable(), id);
            }
        }
    }
}
