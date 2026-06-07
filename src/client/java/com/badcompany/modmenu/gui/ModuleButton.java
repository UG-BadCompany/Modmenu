package com.badcompany.modmenu.gui;

import com.badcompany.modmenu.config.ConfigManager;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleStatus;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.ColorSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import com.badcompany.modmenu.settings.Setting;
import com.badcompany.modmenu.settings.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class ModuleButton {
    private final Module module;
    private boolean expanded;
    private boolean binding;

    public ModuleButton(Module module) { this.module = module; }
    public Module module() { return module; }
    public boolean expanded() { return expanded; }

    public int fullHeight(ConfigManager configManager) {
        return rowHeight(configManager) + (expanded ? module.settings().size() * settingHeight(configManager) + keybindHeight(configManager) : 0);
    }

    public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, ConfigManager configManager) {
        MinecraftClient client = MinecraftClient.getInstance();
        int rowHeight = rowHeight(configManager);
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + rowHeight;
        int rowColor = hovered ? ClickGuiScreen.alpha(configManager.accentColor(), 34) : ((module.enabled() ? ClickGuiScreen.alpha(configManager.accentColor(), 18) : 0x00000000));
        if (module.status() == ModuleStatus.UNSAFE_DISABLED) rowColor = hovered ? 0x55331111 : 0x22180000;
        if (rowColor != 0) context.fill(x, y, x + width, y + rowHeight, rowColor);

        int labelColor = module.status() == ModuleStatus.UNSAFE_DISABLED ? configManager.warningColor() : module.enabled() ? configManager.enabledModuleColor() : configManager.disabledModuleColor();
        context.fill(x + 3, y + 3, x + 5, y + rowHeight - 3, module.status().color());
        context.drawText(client.textRenderer, CategoryPanel.truncate(module.name(), width - 58), x + 8, y + centeredText(rowHeight), labelColor, false);
        drawStatusDot(context, x + width - 39, y + rowHeight / 2 - 2, module.status());
        drawToggle(context, x + width - 28, y + Math.max(2, (rowHeight - 8) / 2), module.enabled(), module.status(), configManager);
        if (!module.settings().isEmpty()) {
            context.drawText(client.textRenderer, expanded ? "▾" : "▸", x + width - 8, y + centeredText(rowHeight), configManager.textColor(), false);
        }

        if (expanded) renderSettings(context, x, y + rowHeight, width, mouseX, mouseY, configManager);
    }

    private void renderSettings(DrawContext context, int x, int y, int width, int mouseX, int mouseY, ConfigManager configManager) {
        MinecraftClient client = MinecraftClient.getInstance();
        int settingHeight = settingHeight(configManager);
        int settingY = y;
        for (Setting<?> setting : module.settings()) {
            boolean hovered = mouseX >= x + 6 && mouseX <= x + width - 5 && mouseY >= settingY && mouseY <= settingY + settingHeight;
            context.fill(x + 6, settingY, x + width - 5, settingY + settingHeight, hovered ? ClickGuiScreen.alpha(configManager.accentColor(), 30) : ClickGuiScreen.alpha(configManager.backgroundColor(), 115));
            context.fill(x + 7, settingY, x + 8, settingY + settingHeight, ClickGuiScreen.alpha(configManager.borderColor(), 150));
            renderSettingValue(context, setting, x + 12, settingY, width - 21, settingHeight, configManager);
            settingY += settingHeight;
        }
        int keyHeight = keybindHeight(configManager);
        String keyLabel = binding ? "Press a key..." : "Bind: " + module.keybind().getLocalizedText().getString();
        context.fill(x + 6, settingY, x + width - 5, settingY + keyHeight, ClickGuiScreen.alpha(configManager.backgroundColor(), 130));
        context.drawText(client.textRenderer, CategoryPanel.truncate(keyLabel, width - 22), x + 12, settingY + centeredText(keyHeight), binding ? configManager.accentColor() : configManager.disabledModuleColor(), false);
    }

    private void renderSettingValue(DrawContext context, Setting<?> setting, int x, int y, int width, int height, ConfigManager configManager) {
        MinecraftClient client = MinecraftClient.getInstance();
        int labelColor = configManager.textColor();
        if (setting instanceof BooleanSetting booleanSetting) {
            context.drawText(client.textRenderer, CategoryPanel.truncate(setting.name(), width - 32), x, y + centeredText(height), labelColor, false);
            drawMiniToggle(context, x + width - 24, y + Math.max(2, (height - 7) / 2), booleanSetting.get(), configManager);
        } else if (setting instanceof NumberSetting numberSetting) {
            int valueWidth = 34;
            String value = compactNumber(numberSetting.get());
            context.drawText(client.textRenderer, CategoryPanel.truncate(setting.name(), width - valueWidth - 8), x, y + 1, labelColor, false);
            int barX = x;
            int barY = y + height - 4;
            int barWidth = Math.max(20, width - valueWidth - 8);
            double progress = (numberSetting.get() - numberSetting.min()) / Math.max(0.001D, numberSetting.max() - numberSetting.min());
            context.fill(barX, barY, barX + barWidth, barY + 2, ClickGuiScreen.alpha(configManager.borderColor(), 130));
            context.fill(barX, barY, barX + (int) (barWidth * progress), barY + 2, configManager.accentColor());
            context.drawText(client.textRenderer, value, x + width - valueWidth, y + centeredText(height), configManager.accentColor(), false);
        } else if (setting instanceof ColorSetting colorSetting) {
            context.drawText(client.textRenderer, CategoryPanel.truncate(setting.name(), width - 56), x, y + centeredText(height), labelColor, false);
            int swatch = colorSetting.get();
            context.fill(x + width - 50, y + 2, x + width - 30, y + height - 2, swatch);
            ClickGuiScreen.drawBorder(context, x + width - 50, y + 2, 20, height - 4, configManager.borderColor());
            context.drawText(client.textRenderer, colorSetting.hex().substring(3), x + width - 27, y + centeredText(height), configManager.disabledModuleColor(), false);
        } else if (setting instanceof StringSetting stringSetting) {
            context.drawText(client.textRenderer, CategoryPanel.truncate(setting.name() + ": " + stringSetting.get(), width - 4), x, y + centeredText(height), labelColor, false);
        } else {
            context.drawText(client.textRenderer, CategoryPanel.truncate(setting.name() + ": " + setting.get(), width - 4), x, y + centeredText(height), labelColor, false);
        }
    }

    public boolean mouseClicked(int relativeY, int button, ConfigManager configManager) {
        if (relativeY < 0 || relativeY > fullHeight(configManager)) return false;
        if (relativeY <= rowHeight(configManager)) {
            if (button == 0) module.toggle();
            if (button == 1 && !module.settings().isEmpty()) expanded = !expanded;
            return true;
        }
        if (!expanded) return false;

        int settingArea = module.settings().size() * settingHeight(configManager);
        int offset = relativeY - rowHeight(configManager);
        if (offset >= settingArea) {
            if (button == 0) binding = true;
            if (button == 1) module.setKeybind(net.minecraft.client.util.InputUtil.UNKNOWN_KEY);
            return true;
        }
        int index = offset / settingHeight(configManager);
        if (index < 0 || index >= module.settings().size()) return false;

        Setting<?> setting = module.settings().get(index);
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.toggle();
        } else if (setting instanceof NumberSetting numberSetting) {
            cycleNumber(numberSetting, button == 1);
        } else if (setting instanceof ColorSetting colorSetting) {
            if (button == 1) colorSetting.reverseCycle(); else colorSetting.cycle();
        } else if (setting instanceof StringSetting stringSetting && button == 0) {
            stringSetting.set(stringSetting.get().isBlank() ? "value" : "");
        }
        return true;
    }

    private static int rowHeight(ConfigManager configManager) {
        return configManager.rowHeightScaled();
    }

    private static int settingHeight(ConfigManager configManager) {
        return Math.max(11, (int) Math.round((configManager.compactMode() ? 12 : 15) * configManager.guiScale()));
    }

    private static int keybindHeight(ConfigManager configManager) {
        return Math.max(11, (int) Math.round(12 * configManager.guiScale()));
    }

    private static int centeredText(int height) {
        return Math.max(1, (height - 8) / 2);
    }

    private static void drawToggle(DrawContext context, int x, int y, boolean enabled, ModuleStatus status, ConfigManager configManager) {
        int color = status == ModuleStatus.UNSAFE_DISABLED ? configManager.warningColor() : enabled ? configManager.enabledModuleColor() : configManager.disabledModuleColor();
        context.fill(x, y, x + 20, y + 8, ClickGuiScreen.alpha(configManager.backgroundColor(), 220));
        ClickGuiScreen.drawBorder(context, x, y, 20, 8, color);
        context.fill(enabled ? x + 12 : x + 2, y + 2, enabled ? x + 18 : x + 8, y + 6, color);
    }

    private static void drawMiniToggle(DrawContext context, int x, int y, boolean enabled, ConfigManager configManager) {
        int color = enabled ? configManager.enabledModuleColor() : configManager.disabledModuleColor();
        context.fill(x, y, x + 18, y + 7, ClickGuiScreen.alpha(configManager.backgroundColor(), 210));
        ClickGuiScreen.drawBorder(context, x, y, 18, 7, color);
        context.fill(enabled ? x + 11 : x + 2, y + 2, enabled ? x + 16 : x + 7, y + 5, color);
    }

    private static void drawStatusDot(DrawContext context, int x, int y, ModuleStatus status) {
        context.fill(x, y, x + 4, y + 4, status.color());
    }

    private static void cycleNumber(NumberSetting setting, boolean backwards) {
        double range = Math.max(1.0D, setting.max() - setting.min());
        double step = range <= 10 ? 1.0D : range <= 64 ? 4.0D : Math.ceil(range / 20.0D);
        double next = setting.get() + (backwards ? -step : step);
        if (next > setting.max()) next = setting.min();
        if (next < setting.min()) next = setting.max();
        setting.set(next);
    }

    private static String compactNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
