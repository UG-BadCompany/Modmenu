package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.NumberSetting;

public final class WorldHistoryViewerModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting sampleInterval = addSetting(new NumberSetting("History interval", "Ticks between visited-world history statistics samples.", 100.0D, 20.0D, 1200.0D));
    private int cooldown;
    public WorldHistoryViewerModule() { super("World History Viewer", "Summarizes first visit, last visit, changes, players seen, and structures discovered from the hunt database.", Category.HUNTING); }
    @Override public void tick() {
        database.tickAutosave();
        if (client.player == null || cooldown-- > 0) return;
        cooldown = (int) Math.round(sampleInterval.get());
        database.incrementStatistic("chunks_explored", 1.0D);
        database.recordEvidence(client.player.getBlockPos(), HuntDatabase.dimension(), "world_history_visit", "Visited area for history timeline");
    }
}
