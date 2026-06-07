package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;

public final class WorldHeatmapModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final BooleanSetting playerTraffic = addSetting(new BooleanSetting("Player traffic", "Expose player traffic cells for overlay rendering.", true));
    private final BooleanSetting structures = addSetting(new BooleanSetting("Structures", "Expose portals, storage, bases, and stash cells for overlay rendering.", true));
    private final BooleanSetting trails = addSetting(new BooleanSetting("Trails", "Expose trail density cells for overlay rendering.", true));
    public WorldHeatmapModule() { super("World Heatmap", "Generates heatmap-ready intelligence layers for players, portals, trails, storage, bases, and stashes.", Category.HUNTING); }
    @Override public void tick() { database.tickAutosave(); }
    public boolean playerTraffic() { return playerTraffic.get(); }
    public boolean structures() { return structures.get(); }
    public boolean trails() { return trails.get(); }
}
