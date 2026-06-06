package com.badcompany.modmenu.gui;

import com.badcompany.modmenu.config.ConfigManager;
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
    private final ModuleManager moduleManager;
    private final ConfigManager configManager;
    private final List<CategoryPanel> panels = new ArrayList<>();
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
        openedAt = System.currentTimeMillis();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float animation = Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 140.0F);
        int alpha = (int) (0xAA * animation) << 24;
        context.fill(0, 0, width, height, alpha | 0x08080D);
        context.drawTextWithShadow(textRenderer, "BadCompany", 12, 32, 0xFFFFFFFF);
        searchBox.render(context, mouseX, mouseY, delta);
        String search = searchBox.getText();
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, search, height);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (searchBox.mouseClicked(click, doubled)) return true;
        for (CategoryPanel panel : panels) if (panel.mouseClicked(click.x(), click.y(), click.button())) return true;
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
}
