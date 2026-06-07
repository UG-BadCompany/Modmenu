package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.StringSetting;

public final class SharedHuntFilesModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final BooleanSetting exportOnEnable = addSetting(new BooleanSetting("Export on enable", "Write a shareable JSON snapshot when enabled.", false));
    private final StringSetting exportName = addSetting(new StringSetting("Export filename", "Shared hunt JSON filename in the BadCompany hunting config folder.", "shared-hunt-export.json"));
    public SharedHuntFilesModule() { super("Shared Hunt Files", "Exports local discoveries and safely merges imported intelligence files through the Hunt Database module.", Category.HUNTING); }
    @Override protected void onEnable() { if (exportOnEnable.get()) database.exportSafely(exportName.get()); }
    @Override public void tick() { database.tickAutosave(); }
}
