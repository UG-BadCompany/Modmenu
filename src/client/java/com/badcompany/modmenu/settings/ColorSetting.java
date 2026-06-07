package com.badcompany.modmenu.settings;

public final class ColorSetting extends Setting<Integer> {
    private static final int[] PRESETS = {
            0xFF55FFFF, 0xFF55FF55, 0xFFFFFF55, 0xFFFF5555, 0xFFFF55FF, 0xFFFFFFFF
    };

    public ColorSetting(String name, String description, int value) {
        super(name, description, value);
    }

    public void cycle() {
        int current = get() == null ? PRESETS[0] : get();
        for (int i = 0; i < PRESETS.length; i++) {
            if (PRESETS[i] == current) {
                set(PRESETS[(i + 1) % PRESETS.length]);
                return;
            }
        }
        set(PRESETS[0]);
    }

    public String hex() {
        return String.format("#%08X", get());
    }
}
