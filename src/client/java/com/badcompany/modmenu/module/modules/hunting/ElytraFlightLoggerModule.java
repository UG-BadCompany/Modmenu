package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.util.math.Vec3d;

public final class ElytraFlightLoggerModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting sampleInterval = addSetting(new NumberSetting("Sample interval", "Ticks between flight path samples.", 20.0D, 1.0D, 200.0D));
    private Vec3d previous;
    private int cooldown;
    public ElytraFlightLoggerModule() { super("Elytra Flight Logger", "Tracks distance, average speed, and replayable long-distance flight path samples.", Category.HUNTING); }
    @Override protected void onDisable() { previous = null; }
    @Override public void tick() {
        database.tickAutosave();
        if (client.player == null) { previous = null; return; }
        if (cooldown-- > 0) return;
        cooldown = (int) Math.round(sampleInterval.get());
        Vec3d now = new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ());
        double speed = previous == null ? 0.0D : now.distanceTo(previous) / Math.max(1.0D, sampleInterval.get());
        if (client.player.isGliding()) {
            database.recordFlightPoint(now, HuntDatabase.dimension(), speed, 0);
            database.incrementStatistic("elytra_distance", previous == null ? 0.0D : now.distanceTo(previous));
        }
        previous = now;
    }
}
