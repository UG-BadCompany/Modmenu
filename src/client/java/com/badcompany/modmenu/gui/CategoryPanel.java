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
    private static final int BASE_HEADER_HEIGHT = 13;
    private final Category category;
    private final List<ModuleButton> buttons = new ArrayList<>();
    private int x;
    private int y;
    private boolean expanded = true;
    private boolean dragging;
    private int dragX;
    private int dragY;
    private int scroll;
    private String lastSearch = "";

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

    public void apply(ConfigManager.PanelConfig config) {
        if (config == null) return;
        x = config.x;
        y = config.y;
        expanded = config.expanded;
    }

    public void render(DrawContext context, int mouseX, int mouseY, String search, int screenHeight, ConfigManager configManager) {
        MinecraftClient client = MinecraftClient.getInstance();
        lastSearch = search == null ? "" : search;
        int width = width(configManager);
        int headerHeight = headerHeight(configManager);
        int accent = configManager.accentColor();
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }
        x = Math.max(4, Math.min(x, Math.max(4, client.getWindow().getScaledWidth() - width - 4)));
        y = Math.max(4, Math.min(y, Math.max(4, screenHeight - headerHeight - 4)));

        int visibleHeight = expanded ? Math.min(panelContentHeight(configManager), Math.max(headerHeight, screenHeight - y - 6)) : headerHeight;
        context.fill(x, y, x + width, y + visibleHeight, (configManager.backgroundOpacity() << 24));
        drawBorder(context, x, y, width, visibleHeight, configManager.borderColor());
        context.fill(x + 1, y + 1, x + width - 1, y + headerHeight, configManager.categoryHeaderColor(category.name()));
        context.drawText(client.textRenderer, category.displayName(), x + 4, y + 3, 0xFFEEEEEE, false);
        context.drawText(client.textRenderer, expanded ? "-" : "+", x + width - 9, y + 3, 0xFFEEEEEE, false);
        if (!expanded) return;

        int contentY = y + headerHeight + scroll;
        int clipBottom = Math.min(screenHeight - 6, y + visibleHeight - 2);
        context.enableScissor(x + 2, y + headerHeight, x + width - 2, clipBottom);
        for (ModuleButton button : buttons) {
            if (!matches(button, search)) continue;
            button.render(context, x, contentY, width, mouseX, mouseY, configManager);
            contentY += button.fullHeight(configManager) + 1;
        }
        context.disableScissor();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, ConfigManager configManager) {
        int width = width(configManager);
        int headerHeight = headerHeight(configManager);
        if (mouseX < x || mouseX > x + width || mouseY < y) return false;
        if (mouseY <= y + headerHeight) {
            if (button == 0) {
                dragging = true;
                dragX = (int) mouseX - x;
                dragY = (int) mouseY - y;
            }
            if (button == 1) expanded = !expanded;
            return true;
        }
        if (!expanded) return false;
        int currentY = y + headerHeight + scroll;
        for (ModuleButton moduleButton : buttons) {
            if (!matches(moduleButton, lastSearch)) continue;
            int h = moduleButton.fullHeight(configManager) + 1;
            if (mouseY >= currentY && mouseY <= currentY + h) return moduleButton.mouseClicked((int) mouseY - currentY, button, configManager);
            currentY += h;
        }
        return false;
    }

    public void mouseReleased() { dragging = false; }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount, ConfigManager configManager) {
        if (mouseX < x || mouseX > x + width(configManager) || mouseY < y || !expanded) return false;
        scroll = Math.min(0, scroll + (int) (amount * 10));
        return true;
    }

    private int width(ConfigManager configManager) {
        return Math.max(104, (int) Math.round(configManager.panelWidth() * configManager.guiScale()));
    }

    private int headerHeight(ConfigManager configManager) {
        return Math.max(11, (int) Math.round(BASE_HEADER_HEIGHT * configManager.guiScale()));
    }

    private int panelContentHeight(ConfigManager configManager) {
        int contentHeight = headerHeight(configManager) + 2;
        for (ModuleButton button : buttons) {
            if (matches(button, lastSearch)) contentHeight += button.fullHeight(configManager);
        }
        return Math.max(headerHeight(configManager), contentHeight + 2);
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static boolean matches(ModuleButton button, String search) {
        return search == null || search.isBlank() || button.module().name().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }
}
