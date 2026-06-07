package com.badcompany.modmenu.gui;

import com.badcompany.modmenu.config.ConfigManager;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleStatus;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.ColorSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import com.badcompany.modmenu.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class ModuleButton {
    private static final int BASE_HEIGHT = 13;
    private static final int TOGGLE_WIDTH = 16;
    private static final int TOGGLE_HEIGHT = 7;
    private final Module module;
    private boolean expanded;

    public ModuleButton(Module module) { this.module = module; }
    public Module module() { return module; }
    public boolean expanded() { return expanded; }

    public int fullHeight(ConfigManager configManager) {
        return height(configManager) + (expanded ? module.settings().size() * settingHeight(configManager) : 0);
    }

    public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, ConfigManager configManager) {
        MinecraftClient client = MinecraftClient.getInstance();
        int height = height(configManager);
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        int rowColor = hovered ? 0x33111111 : 0x00000000;
        if (module.status() == ModuleStatus.UNSAFE_DISABLED) rowColor = hovered ? 0x44331111 : 0x22180000;
        if (rowColor != 0) context.fill(x + 2, y, x + width - 2, y + height, rowColor);

        int labelColor = module.status() == ModuleStatus.UNSAFE_DISABLED ? 0xFFFFB0B0 : 0xFFEEEEEE;
        context.drawText(client.textRenderer, module.name(), x + 6, y + 2, labelColor, false);
        drawLegacyToggle(context, x + width - 22, y + 3, module.enabled(), module.status(), configManager.accentColor());
        if (!module.settings().isEmpty()) {
            context.drawText(client.textRenderer, expanded ? "-" : "+", x + width - 6, y + 2, 0xFFBBBBBB, false);
        }

        if (expanded) {
            int settingY = y + height;
            for (Setting<?> setting : module.settings()) {
                context.fill(x + 4, settingY, x + width - 4, settingY + settingHeight(configManager), 0x55000000);
                String value = setting.name() + ": " + displayValue(setting);
                int maxChars = Math.max(16, (width - 14) / 6);
                if (value.length() > maxChars) value = value.substring(0, maxChars - 3) + "...";
                context.drawText(client.textRenderer, Text.literal(value), x + 8, settingY + 2, 0xFFD8D8D8, false);
                settingY += settingHeight(configManager);
            }
        }
    }

    public boolean mouseClicked(int relativeY, int button, ConfigManager configManager) {
        if (relativeY < 0 || relativeY > fullHeight(configManager)) return false;
        if (relativeY <= height(configManager)) {
            if (button == 0) module.toggle();
            if (button == 1 && !module.settings().isEmpty()) expanded = !expanded;
            return true;
        }
        if (!expanded || (button != 0 && button != 1)) return false;

        int index = (relativeY - height(configManager)) / settingHeight(configManager);
        if (index < 0 || index >= module.settings().size()) return false;

        Setting<?> setting = module.settings().get(index);
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.toggle();
        } else if (setting instanceof NumberSetting numberSetting) {
            cycleNumber(numberSetting, button == 1);
        } else if (setting instanceof ColorSetting colorSetting) {
            colorSetting.cycle();
        }
        return true;
    }

    private static int height(ConfigManager configManager) {
        int base = configManager.compactMode() ? BASE_HEIGHT : 16;
        return Math.max(11, (int) Math.round(base * configManager.guiScale()));
    }

    private static int settingHeight(ConfigManager configManager) {
        int base = configManager.compactMode() ? 11 : 13;
        return Math.max(10, (int) Math.round(base * configManager.guiScale()));
    }

    private static void drawLegacyToggle(DrawContext context, int x, int y, boolean enabled, ModuleStatus status, int accent) {
        int color = status == ModuleStatus.UNSAFE_DISABLED ? 0xFFFF453A : enabled ? accent : 0xFFBBBBBB;
        int background = enabled ? 0xCC000000 | (accent & 0x00FFFFFF) : 0xD8000000;
        context.fill(x, y, x + TOGGLE_WIDTH, y + TOGGLE_HEIGHT, background);
        drawBorder(context, x, y, TOGGLE_WIDTH, TOGGLE_HEIGHT, color);
        context.fill(enabled ? x + 9 : x + 2, y + 2, enabled ? x + 14 : x + 7, y + 5, color);
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static void cycleNumber(NumberSetting setting, boolean backwards) {
        double step = setting.max() <= 10 ? 1.0D : 4.0D;
        double next = setting.get() + (backwards ? -step : step);
        if (next > setting.max()) next = setting.min();
        if (next < setting.min()) next = setting.max();
        setting.set(next);
    }

    private static String displayValue(Setting<?> setting) {
        if (setting instanceof ColorSetting colorSetting) return colorSetting.hex();
        if (setting instanceof NumberSetting numberSetting) return String.valueOf(Math.round(numberSetting.get()));
        return String.valueOf(setting.get());
    }
}
