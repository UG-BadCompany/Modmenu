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
        int accent = configManager.accentColor();
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        int background = module.enabled() ? withAlpha(accent, 0xCC) : hovered ? 0xCC222222 : 0x99000000;
        if (module.status() == ModuleStatus.UNSAFE_DISABLED) background = hovered ? 0xCC331111 : 0x99180000;

        context.fill(x, y, x + width, y + height, background);
        drawBorder(context, x, y, width, height, module.enabled() ? accent : 0xFFBBBBBB);
        int labelColor = module.status() == ModuleStatus.UNSAFE_DISABLED ? 0xFFFFB0B0 : 0xFFEEEEEE;
        context.drawText(client.textRenderer, module.name(), x + 4, y + 3, labelColor, false);

        String status = compactStatus(module.status());
        int statusWidth = client.textRenderer.getWidth(status);
        int statusColor = module.status().color();
        context.drawText(client.textRenderer, status, x + width - statusWidth - 14, y + 3, statusColor, false);
        context.drawText(client.textRenderer, expanded ? "-" : "+", x + width - 9, y + 3, 0xFFEEEEEE, false);

        if (expanded) {
            int settingY = y + height;
            for (Setting<?> setting : module.settings()) {
                context.fill(x + 2, settingY, x + width - 2, settingY + settingHeight(configManager), 0xCC111111);
                String value = setting.name() + ": " + displayValue(setting);
                int maxChars = Math.max(18, (width - 12) / 6);
                if (value.length() > maxChars) value = value.substring(0, maxChars - 3) + "...";
                context.drawText(client.textRenderer, Text.literal(value), x + 5, settingY + 2, 0xFFD8D8D8, false);
                settingY += settingHeight(configManager);
            }
        }
    }

    public boolean mouseClicked(int relativeY, int button, ConfigManager configManager) {
        if (relativeY < 0 || relativeY > fullHeight(configManager)) return false;
        if (relativeY <= height(configManager)) {
            if (button == 0) module.toggle();
            if (button == 1) expanded = !expanded;
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

    private static String compactStatus(ModuleStatus status) {
        return switch (status) {
            case WORKING -> "Working";
            case PARTIAL -> "Partial";
            case PLACEHOLDER -> "Place";
            case UNSAFE_DISABLED -> "Unsafe";
        };
    }

    private static int withAlpha(int argb, int alpha) {
        return (alpha << 24) | (argb & 0x00FFFFFF);
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
