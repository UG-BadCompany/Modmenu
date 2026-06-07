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
        int panelWidth = Math.max(112, (int) Math.round(configManager.panelWidth() * configManager.guiScale()));
        int x = LEGACY_X;
        int y = 36;
        for (Category category : Category.values()) {
            if (moduleManager.modules(category).isEmpty()) continue;
            CategoryPanel panel = new CategoryPanel(category, moduleManager.modules(category), x, y);
            ConfigManager.PanelConfig saved = configManager.config().gui.panels.get(category.name());
            panel.apply(saved);
            panels.add(panel);
            x += panelWidth + 8;
            if (x > width - panelWidth - 8) {
                x = LEGACY_X;
                y += 112;
            }
        }
        searchBox = new TextFieldWidget(textRenderer, LEGACY_X + 2, 21, Math.min(134, width - 24), 12, Text.literal("Search modules"));
        searchBox.setPlaceholder(Text.literal("Search..."));
        searchBox.setMaxLength(64);
        rebuildControls();
        openedAt = System.currentTimeMillis();
    }

    private void rebuildControls() {
        controls.clear();
        int x = LEGACY_X + 150;
        int y = 20;
        controls.add(new ControlButton(x, y, 84, "Scale: " + trimScale(configManager.guiScale()), () -> configManager.cycleGuiScale()));
        x += 88;
        controls.add(new ControlButton(x, y, 86, "Accent", () -> configManager.cycleAccentColor()));
        x += 90;
        controls.add(new ControlButton(x, y, 82, "Width: " + configManager.panelWidth(), () -> configManager.cyclePanelWidth()));
        x += 86;
        controls.add(new ControlButton(x, y, 86, configManager.compactMode() ? "Compact" : "Roomy", () -> configManager.toggleCompactMode()));
        x += 90;
        controls.add(new ControlButton(x, y, 78, "Reset", () -> {
            configManager.resetGuiLayout();
            init();
        }));
        x += 82;
        controls.add(new ControlButton(x, y, 78, "BG: " + configManager.guiBackground().label(), () -> configManager.cycleGuiBackground()));
        x += 82;
        controls.add(new ControlButton(x, y, 86, "Dashboard", () -> MinecraftClient.getInstance().setScreen(new IntelligenceDashboardScreen(this))));
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
        int headerWidth = Math.min(width - 24, 720);
        context.fill(LEGACY_X, LEGACY_Y, LEGACY_X + headerWidth, 36, 0xD8000000);
        drawBorder(context, LEGACY_X, LEGACY_Y, headerWidth, 24, configManager.accentColor());
        context.drawText(textRenderer, "Family Fun Pack / BadCompany", LEGACY_X + 5, LEGACY_Y + 5, 0xFFEEEEEE, false);
    }

    private void renderConfiguredBackground(DrawContext context, float animation) {
        GuiBackground background = configManager.guiBackground();
        int alpha = switch (background) {
            case NONE -> 0;
            case LIGHT_DIM -> (int) (0x24 * animation);
            case DARK_DIM -> (int) (0x58 * animation);
            case BLUR -> (int) (0x40 * animation);
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
            drawBorder(context, x, y, width, CONTROL_HEIGHT, configManager.accentColor());
            context.drawText(textRenderer, label, x + 3, y + 3, 0xFFEEEEEE, false);
        }

        private boolean clicked(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + CONTROL_HEIGHT;
        }
    }
}
