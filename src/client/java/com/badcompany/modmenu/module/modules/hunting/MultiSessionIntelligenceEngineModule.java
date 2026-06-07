package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.util.math.BlockPos;

public final class MultiSessionIntelligenceEngineModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting analysisInterval = addSetting(new NumberSetting("Analysis interval", "Ticks between multi-session correlation passes.", 600.0D, 100.0D, 2400.0D));
    private final NumberSetting radius = addSetting(new NumberSetting("Area radius", "Estimated likely active base radius.", 512.0D, 64.0D, 4096.0D));
    private int cooldown;

    public MultiSessionIntelligenceEngineModule() {
        super("Multi-Session Intelligence Engine", "Combines players, portals, trails, stashes, and chunk changes into Likely Active Base Area records.", Category.HUNTING);
    }

    @Override
    public void tick() {
        database.tickAutosave();
        if (cooldown-- > 0) return;
        cooldown = (int) Math.round(analysisInterval.get());
        analyze();
    }

    private void analyze() {
        HuntDatabase.Data data = database.data();
        data.stashes.forEach(stash -> {
            long nearbyPlayers = data.players.stream().filter(player -> close(player.x(), player.z(), stash.x(), stash.z())).count();
            long nearbyPortals = data.portals.stream().filter(portal -> close(portal.x(), portal.z(), stash.x(), stash.z())).count();
            long nearbyTrails = data.trails.stream().filter(trail -> close(trail.x(), trail.z(), stash.x(), stash.z())).count();
            long nearbyChanges = data.chunkChanges.stream().filter(change -> close(change.x(), change.z(), stash.x(), stash.z())).count();
            int confidence = (int) Math.min(100, stash.confidence() + nearbyPlayers * 10 + nearbyPortals * 10 + nearbyTrails * 3 + nearbyChanges * 15);
            if (confidence >= 70) {
                database.recordBase(new BlockPos(stash.x(), stash.y(), stash.z()), stash.dimension(), confidence, (int) Math.round(radius.get()),
                        "Likely Active Base Area: storage + player/portal/trail/change correlation");
            }
        });
    }

    private boolean close(double ax, double az, double bx, double bz) {
        double dx = ax - bx;
        double dz = az - bz;
        double r = radius.get();
        return dx * dx + dz * dz <= r * r;
    }
}
