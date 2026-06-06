package com.badcompany.modmenu.gui;

import com.badcompany.modmenu.config.ConfigManager;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CategoryPanel {
    private static final int HEADER_HEIGHT = 22;
    private static final int WIDTH = 138;
    private final Category category;
    private final List<ModuleButton> buttons = new ArrayList<>();
    private int x;
    private int y;
    private boolean expanded = true;
    private boolean dragging;
    private int dragX;
    private int dragY;
    private int scroll;

    public CategoryPanel(Category category, List<Module> modules, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
        modules.forEach(module -> buttons.add(new ModuleButton(module)));
    }

    public Category category() { return category; }
    public int x() { return x; }
    public int y() { return y; }
    public boolean expanded() { return expanded; }
    public void apply(ConfigManager.PanelConfig config) { if (config != null) { x = config.x; y = config.y; expanded = config.expanded; } }

    public void render(DrawContext context, int mouseX, int mouseY, String search, int screenHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }
        x = Math.max(4, Math.min(x, Math.max(4, MinecraftClient.getInstance().getWindow().getScaledWidth() - WIDTH - 4)));
        y = Math.max(4, Math.min(y, Math.max(4, screenHeight - HEADER_HEIGHT - 4)));
        context.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, 0xEE17171E);
        context.fill(x, y + HEADER_HEIGHT - 2, x + WIDTH, y + HEADER_HEIGHT, 0xFF7C4DFF);
        context.drawTextWithShadow(client.textRenderer, category.name(), x + 7, y + 7, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, expanded ? "▾" : "▸", x + WIDTH - 14, y + 7, 0xFFFFFFFF);
        if (!expanded) return;
        int contentY = y + HEADER_HEIGHT + scroll;
        int clipBottom = screenHeight - 8;
        context.enableScissor(x, y + HEADER_HEIGHT, x + WIDTH, clipBottom);
        for (ModuleButton button : buttons) {
            if (!matches(button, search)) continue;
            button.render(context, x, contentY, WIDTH, mouseX, mouseY);
            contentY += button.fullHeight() + 2;
        }
        context.disableScissor();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX > x + WIDTH || mouseY < y) return false;
        if (mouseY <= y + HEADER_HEIGHT) {
            if (button == 0) { dragging = true; dragX = (int) mouseX - x; dragY = (int) mouseY - y; }
            if (button == 1) expanded = !expanded;
            return true;
        }
        if (!expanded) return false;
        int currentY = y + HEADER_HEIGHT + scroll;
        for (ModuleButton moduleButton : buttons) {
            int h = moduleButton.fullHeight() + 2;
            if (mouseY >= currentY && mouseY <= currentY + h) return moduleButton.mouseClicked((int) mouseY - currentY, mouseX, x, currentY, WIDTH, button);
            currentY += h;
        }
        return false;
    }

    public void mouseReleased() { dragging = false; }
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX < x || mouseX > x + WIDTH || mouseY < y || !expanded) return false;
        scroll = Math.min(0, scroll + (int) (amount * 12));
        return true;
    }

    private static boolean matches(ModuleButton button, String search) {
        return search == null || search.isBlank() || button.module().name().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }
}
