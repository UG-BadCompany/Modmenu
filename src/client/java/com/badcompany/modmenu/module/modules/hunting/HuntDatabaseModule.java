package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.StringSetting;

public final class HuntDatabaseModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final StringSetting searchQuery = addSetting(new StringSetting("Search query", "Examples: player:Steve, portal:true, dimension:minecraft:the_nether, near:100000,500000.", "portal:true"));
    private final BooleanSetting autosave = addSetting(new BooleanSetting("Autosave", "Flush hunting data periodically and on client shutdown.", true));

    public HuntDatabaseModule() {
        super("Hunt Database", "Persistent local JSON intelligence database for players, portals, signs, books, trails, stashes, bases, and chunk changes.", Category.HUNTING, true);
    }

    @Override
    public void tick() {
        if (autosave.get()) database.tickAutosave();
    }

    @Override
    protected void onDisable() { database.saveSafely(); }
    public String searchQuery() { return searchQuery.get(); }
}
