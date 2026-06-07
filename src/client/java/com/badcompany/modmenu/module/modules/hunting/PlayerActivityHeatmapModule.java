package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerActivityHeatmapModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting sampleInterval = addSetting(new NumberSetting("Sample interval", "Ticks between player activity samples.", 20.0D, 1.0D, 200.0D));
    private final BooleanSetting heatmapOverlay = addSetting(new BooleanSetting("Heatmap overlay", "Expose local player sightings for heatmap overlay rendering.", true));
    private final BooleanSetting routeOverlay = addSetting(new BooleanSetting("Route overlay", "Expose repeated direction samples as frequently traveled routes.", true));
    private final Map<UUID, Vec3d> previous = new HashMap<>();
    private int cooldown;

    public PlayerActivityHeatmapModule() {
        super("Player Activity Heatmap", "Records render-distance player sightings, movement direction, speed, and timestamps into the hunt database.", Category.HUNTING);
    }

    @Override
    protected void onDisable() { previous.clear(); }

    @Override
    public void tick() {
        database.tickAutosave();
        if (client.world == null || client.player == null) { previous.clear(); return; }
        if (cooldown-- > 0) return;
        cooldown = (int) Math.round(sampleInterval.get());
        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;
            Vec3d now = new Vec3d(player.getX(), player.getY(), player.getZ());
            Vec3d old = previous.put(player.getUuid(), now);
            double speed = old == null ? 0.0D : now.distanceTo(old) / Math.max(1.0D, sampleInterval.get());
            double direction = old == null ? player.getYaw() : Math.toDegrees(Math.atan2(now.z - old.z, now.x - old.x));
            database.recordPlayer(player.getName().getString(), player.getUuid(), now, HuntDatabase.dimension(), direction, speed);
        }
    }

    public boolean heatmapOverlay() { return heatmapOverlay.get(); }
    public boolean routeOverlay() { return routeOverlay.get(); }
}
