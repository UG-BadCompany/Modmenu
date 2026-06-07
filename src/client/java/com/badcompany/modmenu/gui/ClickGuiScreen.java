package com.badcompany.modmenu.gui;

import com.badcompany.modmenu.config.ConfigManager;
import com.badcompany.modmenu.config.ConfigManager.GuiBackground;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class ClickGuiScreen extends Screen {
    private static final int LEGACY_X = 12;
    private static final int LEGACY_Y = 12;
    private static final int CONTROL_HEIGHT = 13;
    private static final int LEGACY_PANEL_WIDTH = 126;
    private static final int LEGACY_PANEL_HEIGHT = 176;
    private final ModuleManager moduleManager;
    private final ConfigManager configManager;
    private final List<CategoryPanel> panels = new ArrayList<>();
    private final List<ControlButton> controls = new ArrayList<>();
    private TextFieldWidget searchBox;
    private long openedAt;

    public ClickGuiScreen(ModuleManager moduleManager, ConfigManager configManager) {
        super(Text.literal("BadCompany"));
        this.moduleManager = moduleManager;
        this.configManager = configManager;
    }

    @Override
    protected void init() {
        panels.clear();
        int panelWidth = Math.max(104, (int) Math.round(configManager.panelWidth() * configManager.guiScale()));
        int x = LEGACY_X;
        int y = 48;
        for (Category category : Category.values()) {
            if (moduleManager.modules(category).isEmpty()) continue;
            CategoryPanel panel = new CategoryPanel(category, moduleManager.modules(category), x, y);
            ConfigManager.PanelConfig saved = configManager.config().gui.panels.get(category.name());
            panel.apply(saved);
            panels.add(panel);
            x += panelWidth + 6;
            if (x > width - panelWidth - 6) {
                x = LEGACY_X;
                y += LEGACY_PANEL_HEIGHT + 6;
            }
        }
        searchBox = new TextFieldWidget(textRenderer, LEGACY_X + 4, 33, Math.min(LEGACY_PANEL_WIDTH - 8, width - 24), 12, Text.literal("Search modules"));
        searchBox.setPlaceholder(Text.literal("Search..."));
        searchBox.setMaxLength(64);
        rebuildControls();
        openedAt = System.currentTimeMillis();
    }

    private void rebuildControls() {
        controls.clear();
        int x = LEGACY_X + LEGACY_PANEL_WIDTH + 6;
        int y = LEGACY_Y + 4;
        x = addControl(x, y, 54, "Scale " + trimScale(configManager.guiScale()), () -> configManager.cycleGuiScale());
        x = addControl(x, y, 42, "Font", () -> configManager.cycleFontScale());
        x = addControl(x, y, 54, "Row " + configManager.rowHeight(), () -> configManager.cycleRowHeight());
        x = addControl(x, y, 58, "Width " + configManager.panelWidth(), () -> configManager.cyclePanelWidth());
        x = addControl(x, y, 46, "Accent", () -> configManager.cycleAccentColor());
        x = addControl(x, y, 44, "Border", () -> configManager.cycleBorderColor());
        x = addControl(x, y, 46, "Header", () -> configManager.cycleHeaderColor());
        x = addControl(x, y, 46, "On", () -> configManager.cycleEnabledColor());
        x = addControl(x, y, 46, "Off", () -> configManager.cycleDisabledColor());
        x = addControl(x, y, 54, "Alpha", () -> configManager.cycleBackgroundOpacity());
        x = addControl(x, y, 52, configManager.compactMode() ? "Compact" : "Roomy", () -> configManager.toggleCompactMode());
        x = addControl(x, y, 42, "Reset", () -> { configManager.resetGuiLayout(); init(); });
        x = addControl(x, y, 60, "BG " + configManager.guiBackground().label(), () -> configManager.cycleGuiBackground());
        addControl(x, y, 42, "Intel", () -> MinecraftClient.getInstance().setScreen(new IntelligenceDashboardScreen(this)));
    }

    private int addControl(int x, int y, int buttonWidth, String label, Runnable action) {
        if (x + buttonWidth > width - 8) {
            x = LEGACY_X + LEGACY_PANEL_WIDTH + 6;
            y += CONTROL_HEIGHT + 3;
        }
        controls.add(new ControlButton(x, y, buttonWidth, label, action));
        return x + buttonWidth + 4;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float animation = Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 120.0F);
        renderConfiguredBackground(context, animation);
        renderLegacyHeader(context);
        searchBox.render(context, mouseX, mouseY, delta);
        for (ControlButton control : controls) control.render(context, mouseX, mouseY);
        String search = searchBox.getText();
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, search, height, configManager);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderLegacyHeader(DrawContext context) {
        int headerWidth = Math.min(width - 24, LEGACY_PANEL_WIDTH);
        context.fill(LEGACY_X, LEGACY_Y, LEGACY_X + headerWidth, 47, (configManager.backgroundOpacity() << 24));
        drawBorder(context, LEGACY_X, LEGACY_Y, headerWidth, 35, configManager.borderColor());
        context.drawText(textRenderer, "Family Fun Pack", LEGACY_X + 33, LEGACY_Y + 6, 0xFFEEEEEE, false);
        context.drawText(textRenderer, "BadCompany", LEGACY_X + 45, LEGACY_Y + 18, configManager.accentColor(), false);
    }

    private void renderConfiguredBackground(DrawContext context, float animation) {
        GuiBackground background = configManager.guiBackground();
        int alpha = switch (background) {
            case NONE -> 0;
            case LIGHT_DIM -> (int) (Math.min(0x40, configManager.backgroundOpacity() / 4) * animation);
            case DARK_DIM -> (int) (Math.min(0x90, configManager.backgroundOpacity() / 2) * animation);
            case BLUR -> (int) (Math.min(0x70, configManager.backgroundOpacity() / 3) * animation);
        };
        if (alpha > 0) context.fill(0, 0, width, height, (alpha << 24) | 0x000000);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (searchBox.mouseClicked(click, doubled)) return true;
        if (button == 0) {
            for (ControlButton control : controls) {
                if (control.clicked(mouseX, mouseY)) {
                    control.action.run();
                    configManager.saveSafely();
                    rebuildControls();
                    return true;
                }
            }
        }
        for (CategoryPanel panel : panels) if (panel.mouseClicked(mouseX, mouseY, button, configManager)) return true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        panels.forEach(CategoryPanel::mouseReleased);
        panels.forEach(configManager::rememberPanel);
        configManager.saveSafely();
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : panels) if (panel.mouseScrolled(mouseX, mouseY, verticalAmount, configManager)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(CharInput input) {
        return searchBox.charTyped(input) || super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (searchBox.keyPressed(input)) return true;
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        panels.forEach(configManager::rememberPanel);
        configManager.saveSafely();
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void blur() {
        // Legacy utility-client GUI: keep the world visible behind the compact panels.
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static String trimScale(double value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private final class ControlButton {
        private final int x;
        private final int y;
        private final int width;
        private final String label;
        private final Runnable action;

        private ControlButton(int x, int y, int width, String label, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.label = label;
            this.action = action;
        }

        private void render(DrawContext context, int mouseX, int mouseY) {
            boolean hovered = clicked(mouseX, mouseY);
            context.fill(x, y, x + width, y + CONTROL_HEIGHT, hovered ? 0xCC222222 : 0x99000000);
            drawBorder(context, x, y, width, CONTROL_HEIGHT, configManager.borderColor());
            context.drawText(textRenderer, label, x + 3, y + 3, hovered ? configManager.accentColor() : 0xFFEEEEEE, false);
        }

        private boolean clicked(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + CONTROL_HEIGHT;
        }
    }
}
