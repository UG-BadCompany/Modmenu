package com.badcompany.modmenu.gui;

import com.badcompany.modmenu.config.ConfigManager;
import com.badcompany.modmenu.config.ConfigManager.GuiBackground;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class ClickGuiScreen extends Screen {
    private final ModuleManager moduleManager;
    private final ConfigManager configManager;
    private final List<CategoryPanel> panels = new ArrayList<>();
    private TextFieldWidget searchBox;
    private long openedAt;
    private int backgroundButtonX;
    private int backgroundButtonY;
    private int backgroundButtonWidth;
    private int dashboardButtonX;
    private int dashboardButtonY;
    private int dashboardButtonWidth;

    public ClickGuiScreen(ModuleManager moduleManager, ConfigManager configManager) {
        super(Text.literal("BadCompany"));
        this.moduleManager = moduleManager;
        this.configManager = configManager;
    }

    @Override
    protected void init() {
        panels.clear();
        int x = 14;
        int y = 38;
        for (Category category : Category.values()) {
            if (moduleManager.modules(category).isEmpty()) continue;
            CategoryPanel panel = new CategoryPanel(category, moduleManager.modules(category), x, y);
            ConfigManager.PanelConfig saved = configManager.config().gui.panels.get(category.name());
            panel.apply(saved);
            panels.add(panel);
            x += 150;
            if (x > width - 150) { x = 14; y += 180; }
        }
        searchBox = new TextFieldWidget(textRenderer, 12, 12, Math.min(220, width - 24), 18, Text.literal("Search modules"));
        searchBox.setPlaceholder(Text.literal("Search..."));
        searchBox.setMaxLength(64);
        backgroundButtonWidth = 142;
        dashboardButtonWidth = 162;
        backgroundButtonX = Math.max(12, width - backgroundButtonWidth - 12);
        backgroundButtonY = 12;
        dashboardButtonX = Math.max(12, backgroundButtonX - dashboardButtonWidth - 8);
        dashboardButtonY = 12;
        openedAt = System.currentTimeMillis();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float animation = Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 140.0F);
        renderConfiguredBackground(context, animation);
        context.drawTextWithShadow(textRenderer, "BadCompany", 12, 34, 0xFFFFFFFF);
        renderBackgroundButton(context);
        renderDashboardButton(context);
        searchBox.render(context, mouseX, mouseY, delta);
        String search = searchBox.getText();
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, search, height);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderConfiguredBackground(DrawContext context, float animation) {
        GuiBackground background = configManager.guiBackground();
        int alpha = switch (background) {
            case NONE -> 0;
            case LIGHT_DIM -> (int) (0x35 * animation);
            case DARK_DIM -> (int) (0x72 * animation);
            case BLUR -> (int) (0x4C * animation);
        };
        if (alpha > 0) context.fill(0, 0, width, height, (alpha << 24) | 0x050509);
    }

    private void renderBackgroundButton(DrawContext context) {
        String label = "Background: " + configManager.guiBackground().label();
        context.fill(backgroundButtonX, backgroundButtonY, backgroundButtonX + backgroundButtonWidth, backgroundButtonY + 18, 0xEE17171E);
        context.fill(backgroundButtonX, backgroundButtonY + 16, backgroundButtonX + backgroundButtonWidth, backgroundButtonY + 18, 0xFF7C4DFF);
        context.drawTextWithShadow(textRenderer, label, backgroundButtonX + 6, backgroundButtonY + 5, 0xFFFFFFFF);
    }

    private void renderDashboardButton(DrawContext context) {
        context.fill(dashboardButtonX, dashboardButtonY, dashboardButtonX + dashboardButtonWidth, dashboardButtonY + 18, 0xEE17171E);
        context.fill(dashboardButtonX, dashboardButtonY + 16, dashboardButtonX + dashboardButtonWidth, dashboardButtonY + 18, 0xFFFFD166);
        context.drawTextWithShadow(textRenderer, "Intelligence Dashboard", dashboardButtonX + 6, dashboardButtonY + 5, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (searchBox.mouseClicked(click, doubled)) return true;
        if (button == 0 && mouseX >= backgroundButtonX && mouseX <= backgroundButtonX + backgroundButtonWidth && mouseY >= backgroundButtonY && mouseY <= backgroundButtonY + 18) {
            configManager.cycleGuiBackground();
            configManager.saveSafely();
            return true;
        }
        if (button == 0 && mouseX >= dashboardButtonX && mouseX <= dashboardButtonX + dashboardButtonWidth && mouseY >= dashboardButtonY && mouseY <= dashboardButtonY + 18) {
            MinecraftClient.getInstance().setScreen(new IntelligenceDashboardScreen(this));
            return true;
        }
        for (CategoryPanel panel : panels) if (panel.mouseClicked(mouseX, mouseY, button)) return true;
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
        for (CategoryPanel panel : panels) if (panel.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
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
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void blur() {
        // Utility-client GUI: keep the world crisp behind the panels unless the user selects a dim mode.
    }
}
