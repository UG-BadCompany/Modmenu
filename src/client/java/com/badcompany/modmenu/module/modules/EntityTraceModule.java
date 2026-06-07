package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class EntityTraceModule extends Module {
    private final NumberSetting teleportDistance = addSetting(new NumberSetting(
            "Teleport distance",
            "Minimum entity movement, in blocks per client tick, that is reported as a teleport.",
            32.0D,
            8.0D,
            256.0D
    ));
    private final BooleanSetting includePlayers = addSetting(new BooleanSetting(
            "Include players",
            "Also report player-sized jumps from the client entity cache.",
            false
    ));
    private final BooleanSetting notifyChat = addSetting(new BooleanSetting(
            "Chat notices",
            "Print safe client-side trace notices to local chat.",
            true
    ));
    private final NumberSetting watchRange = addSetting(new NumberSetting(
            "Watch range",
            "Maximum radius of loaded entities to track around the local player.",
            192.0D,
            32.0D,
            512.0D
    ));

    private final Map<UUID, EntitySnapshot> knownEntities = new HashMap<>();
    private int warmupTicks;

    public EntityTraceModule() {
        super("Entity Trace", "Reports large client-side entity position jumps without sending packets.", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        knownEntities.clear();
        warmupTicks = 2;
    }

    @Override
    protected void onDisable() {
        knownEntities.clear();
    }

    @Override
    public void tick() {
        if (client.world == null || client.player == null) {
            knownEntities.clear();
            return;
        }

        Set<UUID> seen = new HashSet<>();
        double range = watchRange.get();
        Box watchBox = client.player.getBoundingBox().expand(range);
        for (Entity entity : client.world.getEntitiesByClass(Entity.class, watchBox, ignored -> true)) {
            if (entity == client.player) continue;
            if (!includePlayers.get() && entity.isPlayer()) continue;

            UUID uuid = entity.getUuid();
            seen.add(uuid);
            EntitySnapshot current = EntitySnapshot.from(entity);
            EntitySnapshot previous = knownEntities.put(uuid, current);
            if (previous == null || warmupTicks > 0) continue;

            double threshold = teleportDistance.get();
            if (previous.squaredDistanceTo(current) >= threshold * threshold) {
                notify(entity.getName().getString() + " moved from " + previous.posText() + " to " + current.posText());
            }
        }
        knownEntities.keySet().removeIf(uuid -> !seen.contains(uuid));
        if (warmupTicks > 0) warmupTicks--;
    }

    private void notify(String message) {
        if (!notifyChat.get() || client.player == null) return;
        client.player.sendMessage(Text.literal("[Entity Trace] " + message), false);
    }

    private record EntitySnapshot(double x, double y, double z, BlockPos blockPos) {
        static EntitySnapshot from(Entity entity) {
            return new EntitySnapshot(entity.getX(), entity.getY(), entity.getZ(), entity.getBlockPos());
        }

        double squaredDistanceTo(EntitySnapshot other) {
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return dx * dx + dy * dy + dz * dz;
        }

        String posText() {
            return blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ();
        }
    }
}
