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
import java.util.Comparator;
import java.util.List;

public final class ClickGuiScreen extends Screen {
    private static final int EDGE = 10;
    private static final int TOP_BAR_HEIGHT = 28;
    private static final int NAV_GAP = 5;
    private static final int BUTTON_HEIGHT = 14;
    private final ModuleManager moduleManager;
    private final ConfigManager configManager;
    private final List<CategoryPanel> panels = new ArrayList<>();
    private final List<NavButton> navButtons = new ArrayList<>();
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
        int panelWidth = scaledPanelWidth();
        int headerBottom = topBarBottom();
        int usableWidth = Math.max(panelWidth, width - EDGE * 2);
        int columns = Math.max(1, Math.min(3, (usableWidth + NAV_GAP) / (panelWidth + NAV_GAP)));
        int[] columnHeights = new int[columns];
        int startX = EDGE;
        if (columns > 1) startX = Math.max(EDGE, (width - (columns * panelWidth + (columns - 1) * NAV_GAP)) / 2);

        for (Category category : orderedCategories()) {
            if (moduleManager.modules(category).isEmpty()) continue;
            int column = shortestColumn(columnHeights);
            int x = startX + column * (panelWidth + NAV_GAP);
            int y = headerBottom + columnHeights[column];
            CategoryPanel panel = new CategoryPanel(category, moduleManager.modules(category), x, y);
            ConfigManager.PanelConfig saved = configManager.config().gui.panels.get(category.name());
            if (saved == null || !saved.userPlaced) {
                panel.setPosition(x, y);
            } else {
                panel.apply(saved);
            }
            panel.clampTo(width, height, configManager, topBarBottom());
            panels.add(panel);
            columnHeights[column] += panel.defaultStackHeight(configManager) + NAV_GAP;
        }

        createTopNavigation();
        openedAt = System.currentTimeMillis();
    }

    private void createTopNavigation() {
        navButtons.clear();
        int left = EDGE + 118;
        int right = width - EDGE;
        int searchWidth = Math.max(80, Math.min(170, right - left - 445));
        int searchX = Math.max(left, right - 445 - searchWidth);
        searchBox = new TextFieldWidget(textRenderer, searchX, EDGE + 8, searchWidth, 12, Text.literal("Search modules"));
        searchBox.setPlaceholder(Text.literal("Search"));
        searchBox.setMaxLength(64);

        int x = searchX + searchWidth + NAV_GAP;
        x = addNavButton(x, 62, "Scale " + trimScale(configManager.guiScale()), () -> { configManager.cycleGuiScale(); init(); });
        x = addNavButton(x, 70, "Theme " + configManager.theme().label(), () -> { configManager.cycleTheme(); init(); });
        x = addNavButton(x, 64, "Profile " + configManager.activeProfile(), () -> configManager.cycleProfile());
        x = addNavButton(x, 74, "Reset Layout", () -> { configManager.resetGuiLayout(); init(); });
        x = addNavButton(x, 54, "HUD", () -> MinecraftClient.getInstance().setScreen(new HudEditorScreen(this, configManager)));
        addNavButton(x, 56, "Intel", () -> MinecraftClient.getInstance().setScreen(new IntelligenceDashboardScreen(this)));
    }

    private int addNavButton(int x, int buttonWidth, String label, Runnable action) {
        if (x + buttonWidth > width - EDGE) return x;
        navButtons.add(new NavButton(x, EDGE + 7, buttonWidth, label, action));
        return x + buttonWidth + NAV_GAP;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float animation = Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 140.0F);
        renderConfiguredBackground(context, animation);
        renderTopBar(context, mouseX, mouseY);
        searchBox.render(context, mouseX, mouseY, delta);
        for (NavButton control : navButtons) control.render(context, mouseX, mouseY);

        List<CategoryPanel> sorted = panels.stream()
                .sorted(Comparator.comparing(CategoryPanel::dragging))
                .toList();
        String search = searchBox.getText();
        for (CategoryPanel panel : sorted) {
            panel.render(context, mouseX, mouseY, search, height, configManager, topBarBottom());
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderTopBar(DrawContext context, int mouseX, int mouseY) {
        int background = alpha(configManager.backgroundColor(), Math.min(245, configManager.backgroundOpacity() + 45));
        context.fill(EDGE, EDGE, width - EDGE, EDGE + TOP_BAR_HEIGHT, background);
        drawBorder(context, EDGE, EDGE, width - EDGE * 2, TOP_BAR_HEIGHT, configManager.borderColor());
        context.fill(EDGE + 1, EDGE + 1, width - EDGE - 1, EDGE + 2, configManager.accentColor());
        context.drawText(textRenderer, "BadCompany", EDGE + 8, EDGE + 6, configManager.textColor(), false);
        context.drawText(textRenderer, "utility client", EDGE + 8, EDGE + 17, configManager.disabledModuleColor(), false);
        if (mouseY < topBarBottom()) {
            context.drawText(textRenderer, "drag panels • right-click modules for settings • scroll panels", EDGE + 4, topBarBottom() - 9, configManager.disabledModuleColor(), false);
        }
    }

    private void renderConfiguredBackground(DrawContext context, float animation) {
        GuiBackground background = configManager.guiBackground();
        int alpha = switch (background) {
            case NONE -> 0;
            case LIGHT_DIM -> (int) (Math.min(0x45, configManager.backgroundOpacity() / 4) * animation);
            case DARK_DIM -> (int) (Math.min(0xA0, configManager.backgroundOpacity() / 2) * animation);
            case BLUR -> (int) (Math.min(0x80, configManager.backgroundOpacity() / 3) * animation);
        };
        if (alpha > 0) context.fill(0, 0, width, height, (alpha << 24));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (searchBox.mouseClicked(click, doubled)) return true;
        if (button == 0) {
            for (NavButton control : navButtons) {
                if (control.clicked(mouseX, mouseY)) {
                    control.action.run();
                    configManager.saveSafely();
                    createTopNavigation();
                    return true;
                }
            }
        }
        for (int i = panels.size() - 1; i >= 0; i--) {
            CategoryPanel panel = panels.get(i);
            if (panel.mouseClicked(mouseX, mouseY, button, configManager)) {
                panels.remove(i);
                panels.add(panel);
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(configManager, width, height, topBarBottom());
            configManager.rememberPanel(panel);
        }
        configManager.saveSafely();
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : panels) if (panel.mouseScrolled(mouseX, mouseY, verticalAmount, height, configManager)) return true;
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
    public void blur() {}

    private int scaledPanelWidth() {
        return Math.max(112, (int) Math.round(configManager.panelWidth() * configManager.guiScale()));
    }

    private int topBarBottom() {
        return EDGE + TOP_BAR_HEIGHT + 12;
    }

    private static List<Category> orderedCategories() {
        return List.of(Category.COMBAT, Category.MOVEMENT, Category.RENDER, Category.PLAYER, Category.WORLD, Category.MISC, Category.EXPLOIT, Category.HUNTING);
    }

    private static int shortestColumn(int[] heights) {
        int column = 0;
        for (int i = 1; i < heights.length; i++) if (heights[i] < heights[column]) column = i;
        return column;
    }

    static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    static int alpha(int argb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (argb & 0x00FFFFFF);
    }

    private static String trimScale(double value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private final class NavButton {
        private final int x;
        private final int y;
        private final int width;
        private final String label;
        private final Runnable action;

        private NavButton(int x, int y, int width, String label, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.label = label;
            this.action = action;
        }

        private void render(DrawContext context, int mouseX, int mouseY) {
            boolean hovered = clicked(mouseX, mouseY);
            context.fill(x, y, x + width, y + BUTTON_HEIGHT, hovered ? alpha(configManager.accentColor(), 80) : alpha(configManager.backgroundColor(), 210));
            drawBorder(context, x, y, width, BUTTON_HEIGHT, hovered ? configManager.accentColor() : configManager.borderColor());
            context.drawText(textRenderer, label, x + 4, y + 3, hovered ? configManager.accentColor() : configManager.textColor(), false);
        }

        private boolean clicked(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + BUTTON_HEIGHT;
        }
    }
}
