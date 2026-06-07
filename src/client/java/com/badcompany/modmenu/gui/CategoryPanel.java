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
    private static final int BASE_HEADER_HEIGHT = 14;
    private static final int MAX_VISIBLE_ROWS = 15;
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
    public boolean dragging() { return dragging; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void apply(ConfigManager.PanelConfig config) {
        if (config == null) return;
        x = config.x;
        y = config.y;
        expanded = config.expanded;
    }

    public void render(DrawContext context, int mouseX, int mouseY, String search, int screenHeight, ConfigManager configManager, int topLimit) {
        MinecraftClient client = MinecraftClient.getInstance();
        lastSearch = search == null ? "" : search;
        int width = width(configManager);
        int headerHeight = headerHeight(configManager);
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }
        clampTo(client.getWindow().getScaledWidth(), screenHeight, configManager, topLimit);

        int contentHeight = contentHeight(configManager);
        int maxHeight = Math.max(headerHeight, screenHeight - y - 8);
        int visibleContent = expanded ? Math.min(contentHeight, Math.min(maxHeight - headerHeight, maxVisibleContent(configManager))) : 0;
        int panelHeight = headerHeight + visibleContent;
        int background = ClickGuiScreen.alpha(configManager.backgroundColor(), configManager.backgroundOpacity());
        context.fill(x, y, x + width, y + panelHeight, background);
        ClickGuiScreen.drawBorder(context, x, y, width, panelHeight, dragging ? configManager.accentColor() : configManager.borderColor());
        context.fill(x + 1, y + 1, x + width - 1, y + headerHeight, configManager.categoryHeaderColor(category.name()));
        context.fill(x + 1, y + headerHeight - 1, x + width - 1, y + headerHeight, configManager.accentColor());

        context.drawText(client.textRenderer, truncate(category.displayName(), width - 34), x + 5, y + 3, configManager.textColor(), false);
        context.drawText(client.textRenderer, String.valueOf(visibleCount(lastSearch)), x + width - 24, y + 3, configManager.disabledModuleColor(), false);
        context.drawText(client.textRenderer, expanded ? "–" : "+", x + width - 9, y + 3, configManager.textColor(), false);
        if (!expanded) return;

        int maxScroll = Math.max(0, contentHeight - visibleContent);
        scroll = Math.max(-maxScroll, Math.min(0, scroll));
        int contentTop = y + headerHeight;
        int contentBottom = y + headerHeight + visibleContent;
        context.enableScissor(x + 1, contentTop, x + width - 1, contentBottom);
        int contentY = contentTop + scroll;
        for (ModuleButton button : buttons) {
            if (!matches(button, lastSearch)) continue;
            int h = button.fullHeight(configManager);
            if (contentY + h >= contentTop && contentY <= contentBottom) {
                button.render(context, x + 1, contentY, width - 2, mouseX, mouseY, configManager);
            }
            contentY += h;
        }
        context.disableScissor();
        if (maxScroll > 0) renderScrollBar(context, x + width - 3, contentTop, visibleContent, maxScroll, configManager);
    }

    private void renderScrollBar(DrawContext context, int barX, int contentTop, int visibleContent, int maxScroll, ConfigManager configManager) {
        context.fill(barX, contentTop + 1, barX + 1, contentTop + visibleContent - 1, ClickGuiScreen.alpha(configManager.borderColor(), 90));
        int thumbHeight = Math.max(12, visibleContent * visibleContent / (visibleContent + maxScroll));
        int travel = Math.max(1, visibleContent - thumbHeight - 2);
        int thumbY = contentTop + 1 + (int) ((-scroll / (double) maxScroll) * travel);
        context.fill(barX - 1, thumbY, barX + 2, thumbY + thumbHeight, configManager.accentColor());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, ConfigManager configManager) {
        int width = width(configManager);
        int headerHeight = headerHeight(configManager);
        int visibleHeight = headerHeight + (expanded ? Math.min(contentHeight(configManager), maxVisibleContent(configManager)) : 0);
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + visibleHeight) return false;
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
            int h = moduleButton.fullHeight(configManager);
            if (mouseY >= currentY && mouseY <= currentY + h) return moduleButton.mouseClicked((int) mouseY - currentY, button, configManager);
            currentY += h;
        }
        return true;
    }

    public void mouseReleased(ConfigManager configManager, int screenWidth, int screenHeight, int topLimit) {
        if (dragging) snapToGrid(configManager);
        dragging = false;
        clampTo(screenWidth, screenHeight, configManager, topLimit);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount, int screenHeight, ConfigManager configManager) {
        int visibleContent = Math.min(contentHeight(configManager), Math.min(maxVisibleContent(configManager), Math.max(0, screenHeight - y - headerHeight(configManager) - 8)));
        if (mouseX < x || mouseX > x + width(configManager) || mouseY < y || mouseY > y + headerHeight(configManager) + visibleContent || !expanded) return false;
        int maxScroll = Math.max(0, contentHeight(configManager) - visibleContent);
        scroll = Math.max(-maxScroll, Math.min(0, scroll + (int) (amount * configManager.scrollStep())));
        return true;
    }

    public void clampTo(int screenWidth, int screenHeight, ConfigManager configManager, int topLimit) {
        int width = width(configManager);
        x = Math.max(4, Math.min(x, Math.max(4, screenWidth - width - 4)));
        y = Math.max(topLimit, Math.min(y, Math.max(topLimit, screenHeight - headerHeight(configManager) - 4)));
    }

    public int defaultStackHeight(ConfigManager configManager) {
        return headerHeight(configManager) + Math.min(contentHeight(configManager), maxVisibleContent(configManager));
    }

    private void snapToGrid(ConfigManager configManager) {
        int grid = Math.max(4, (int) Math.round(6 * configManager.guiScale()));
        x = Math.round(x / (float) grid) * grid;
        y = Math.round(y / (float) grid) * grid;
    }

    private int width(ConfigManager configManager) {
        return Math.max(112, (int) Math.round(configManager.panelWidth() * configManager.guiScale()));
    }

    private int headerHeight(ConfigManager configManager) {
        return Math.max(12, (int) Math.round(BASE_HEADER_HEIGHT * configManager.guiScale()));
    }

    private int contentHeight(ConfigManager configManager) {
        int contentHeight = 1;
        for (ModuleButton button : buttons) {
            if (matches(button, lastSearch)) contentHeight += button.fullHeight(configManager);
        }
        return contentHeight + 1;
    }

    private int maxVisibleContent(ConfigManager configManager) {
        return Math.max(52, configManager.rowHeightScaled() * MAX_VISIBLE_ROWS);
    }

    private int visibleCount(String search) {
        int count = 0;
        for (ModuleButton button : buttons) if (matches(button, search)) count++;
        return count;
    }

    private static boolean matches(ModuleButton button, String search) {
        return search == null || search.isBlank() || button.module().name().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    static String truncate(String text, int pixels) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.textRenderer.getWidth(text) <= pixels) return text;
        String ellipsis = "…";
        int end = text.length();
        while (end > 0 && client.textRenderer.getWidth(text.substring(0, end) + ellipsis) > pixels) end--;
        return end <= 0 ? ellipsis : text.substring(0, end) + ellipsis;
    }
}
