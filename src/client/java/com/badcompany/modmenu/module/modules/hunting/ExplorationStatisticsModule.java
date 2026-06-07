package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.util.math.Vec3d;

public final class ExplorationStatisticsModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting sampleInterval = addSetting(new NumberSetting("Stats interval", "Ticks between lifetime statistics updates.", 20.0D, 1.0D, 200.0D));
    private Vec3d previous;
    private int cooldown;
    public ExplorationStatisticsModule() { super("Exploration Statistics", "Tracks lifetime distance, chunks explored, portals, players, trails, bases, stashes, and hours explored.", Category.HUNTING, true); }
    @Override public void tick() {
        database.tickAutosave();
        if (client.player == null) { previous = null; return; }
        if (cooldown-- > 0) return;
        cooldown = (int) Math.round(sampleInterval.get());
        Vec3d now = new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ());
        if (previous != null) database.incrementStatistic("distance_traveled", now.distanceTo(previous));
        database.incrementStatistic("ticks_explored", sampleInterval.get());
        previous = now;
    }
}
