package com.badcompany.modmenu.module;

public enum ModuleStatus {
    WORKING("Working", 0xFF4CD964),
    PARTIAL("Partial", 0xFFFFCC4D),
    PLACEHOLDER("Placeholder", 0xFFFF8A3D),
    UNSAFE_DISABLED("Unsafe/Disabled", 0xFFFF453A);

    private final String label;
    private final int color;

    ModuleStatus(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public String label() { return label; }
    public int color() { return color; }
}
