package com.badcompany.modmenu.settings;

public final class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;

    public NumberSetting(String name, String description, double value, double min, double max) {
        super(name, description, value);
        this.min = min;
        this.max = max;
    }

    public double min() { return min; }
    public double max() { return max; }

    @Override
    public void set(Double value) {
        super.set(Math.max(min, Math.min(max, value)));
    }
}
