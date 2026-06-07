package com.badcompany.modmenu.settings;

public final class StringSetting extends Setting<String> {
    public StringSetting(String name, String description, String value) {
        super(name, description, value == null ? "" : value);
    }

    @Override
    public void set(String value) {
        super.set(value == null ? "" : value);
    }
}
