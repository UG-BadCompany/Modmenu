package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.util.math.BlockPos;

public final class ExpeditionSystemModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting interval = addSetting(new NumberSetting("Planning interval", "Ticks between automatic expedition generation passes.", 1200.0D, 200.0D, 6000.0D));
    private final NumberSetting radius = addSetting(new NumberSetting("Correlation radius", "Nearby clue radius used to generate investigations.", 2048.0D, 256.0D, 8192.0D));
    private int cooldown;

    public ExpeditionSystemModule() { super("Expedition System", "Generates pinnable investigations from player sightings, trail clusters, portals, and chunk activity.", Category.HUNTING); }

    @Override public void tick() {
        database.tickAutosave();
        if (cooldown-- > 0) return;
        cooldown = (int) Math.round(interval.get());
        HuntDatabase.Data data = database.data();
        data.portals.stream().limit(20).forEach(portal -> {
            long players = data.players.stream().filter(player -> close(player.x(), player.z(), portal.x(), portal.z())).count();
            long trails = data.trails.stream().filter(trail -> close(trail.x(), trail.z(), portal.x(), portal.z())).count();
            long changes = data.chunkChanges.stream().filter(change -> close(change.x(), change.z(), portal.x(), portal.z())).count();
            if (players + trails / 3 + changes >= 3) database.recordExpedition(new BlockPos(portal.x(), portal.y(), portal.z()), portal.dimension(), "Investigate portal-adjacent activity: players=" + players + ", trails=" + trails + ", changes=" + changes, true);
        });
    }

    private boolean close(double ax, double az, double bx, double bz) { double dx = ax - bx; double dz = az - bz; double r = radius.get(); return dx * dx + dz * dz <= r * r; }
}
