package com.badcompany.modmenu.settings;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    private T value;

    protected Setting(String name, String description, T value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public String name() { return name; }
    public String description() { return description; }
    public T get() { return value; }
    public void set(T value) { this.value = value; }
}
