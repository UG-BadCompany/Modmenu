package com.badcompany.modmenu.gui;

import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.ColorSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import com.badcompany.modmenu.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class ModuleButton {
    public static final int HEIGHT = 18;
    private final Module module;
    private boolean expanded;

    public ModuleButton(Module module) { this.module = module; }
    public Module module() { return module; }
    public boolean expanded() { return expanded; }
    public int fullHeight() { return HEIGHT + (expanded ? module.settings().size() * 14 : 0); }

    public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEIGHT;
        int background = module.enabled() ? 0xF05D44D6 : hovered ? 0xF02D2D3A : 0xEA202028;
        context.fill(x - 1, y, x + width + 1, y + HEIGHT, 0xFF050507);
        context.fill(x, y, x + width, y + HEIGHT, background);
        context.drawTextWithShadow(client.textRenderer, module.name(), x + 6, y + 5, module.enabled() ? 0xFFFFFFFF : 0xFFF1F1F1);
        context.drawTextWithShadow(client.textRenderer, expanded ? "−" : "+", x + width - 12, y + 5, 0xFFE0E0E0);
        if (expanded) {
            int settingY = y + HEIGHT;
            for (Setting<?> setting : module.settings()) {
                context.fill(x + 4, settingY, x + width - 4, settingY + 13, 0xE021212A);
                String value = setting.name() + ": " + displayValue(setting);
                if (value.length() > 34) value = value.substring(0, 31) + "...";
                context.drawTextWithShadow(client.textRenderer, Text.literal(value), x + 8, settingY + 3, 0xFFE7E7E7);
                settingY += 14;
            }
        }
    }

    public boolean mouseClicked(int relativeY, int button) {
        if (relativeY < 0 || relativeY > fullHeight()) return false;
        if (relativeY <= HEIGHT) {
            if (button == 0) module.toggle();
            if (button == 1) expanded = !expanded;
            return true;
        }
        if (!expanded || (button != 0 && button != 1)) return false;

        int index = (relativeY - HEIGHT) / 14;
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
