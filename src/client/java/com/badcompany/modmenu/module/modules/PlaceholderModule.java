package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;

public final class PlaceholderModule extends Module {
    private final String migrationNote;

    public PlaceholderModule(String name, String description, Category category, String migrationNote) {
        super(name, description, category);
        this.migrationNote = migrationNote;
        addSetting(new BooleanSetting("Notify", "Show a reminder that this feature is still being ported.", true));
    }

    public String migrationNote() {
        return migrationNote;
    }
}
