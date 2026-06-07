package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleStatus;
import com.badcompany.modmenu.settings.StringSetting;

public final class ClientCommandsModule extends Module {
    private final StringSetting prefix = addSetting(new StringSetting(
            "Prefix",
            "Single-character prefix used for BadCompany client commands.",
            "."
    ));

    public ClientCommandsModule() {
        super("FFP Commands", "Enables the legacy Family Fun Pack-style client command prefix.", Category.MISC, ModuleStatus.WORKING, true);
    }

    public String prefix() {
        String value = prefix.get();
        if (value == null || value.isBlank()) return ".";
        return value.substring(0, 1);
    }
}
