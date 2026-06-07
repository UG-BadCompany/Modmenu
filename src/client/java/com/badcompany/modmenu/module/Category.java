package com.badcompany.modmenu.module;

public enum Category {
    COMBAT("Combat"),
    RENDER("Render"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    WORLD("World"),
    HUNTING("Hunting/Base Hunting"),
    MISC("Misc"),
    EXPLOIT("Exploit");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
