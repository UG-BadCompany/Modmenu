package com.badcompany.modmenu.module;

import com.badcompany.modmenu.BadCompanyClient;
import com.badcompany.modmenu.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    protected final MinecraftClient client = MinecraftClient.getInstance();
    private final String name;
    private final String description;
    private final Category category;
    private final ModuleStatus status;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final boolean enabledByDefault;
    private boolean enabled;
    private InputUtil.Key keybind = InputUtil.UNKNOWN_KEY;

    protected Module(String name, String description, Category category) {
        this(name, description, category, ModuleStatus.PARTIAL, false);
    }

    protected Module(String name, String description, Category category, boolean enabledByDefault) {
        this(name, description, category, ModuleStatus.PARTIAL, enabledByDefault);
    }

    protected Module(String name, String description, Category category, ModuleStatus status) {
        this(name, description, category, status, false);
    }

    protected Module(String name, String description, Category category, ModuleStatus status, boolean enabledByDefault) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status == null ? ModuleStatus.PARTIAL : status;
        this.enabledByDefault = enabledByDefault;
    }

    public final String name() { return name; }
    public final String description() { return description; }
    public final Category category() { return category; }
    public final ModuleStatus status() { return status; }
    public final boolean enabledByDefault() { return enabledByDefault; }
    public final boolean enabled() { return enabled; }
    public final InputUtil.Key keybind() { return keybind; }
    public final List<Setting<?>> settings() { return Collections.unmodifiableList(settings); }

    public final void setKeybind(InputUtil.Key keybind) {
        this.keybind = keybind == null ? InputUtil.UNKNOWN_KEY : keybind;
    }

    public final void setEnabled(boolean enabled) {
        if (enabled && status == ModuleStatus.UNSAFE_DISABLED) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("[BadCompany] " + name + " is marked unsafe/disabled for modern Minecraft."), false);
            }
            this.enabled = false;
            return;
        }
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        try {
            if (enabled) onEnable(); else onDisable();
        } catch (RuntimeException ex) {
            BadCompanyClient.LOGGER.warn("Module '{}' failed while {}", name, enabled ? "enabling" : "disabling", ex);
            this.enabled = false;
        }
    }

    public final void toggle() { setEnabled(!enabled); }

    public void tick() {}
    protected void onEnable() {}
    protected void onDisable() {}

    protected final <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }
}
