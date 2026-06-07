package com.badcompany.modmenu.gui;

import com.badcompany.modmenu.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private final ConfigManager configManager;
    private final List<HudWidget> widgets = new ArrayList<>();
    private HudWidget dragging;
    private int dragX;
    private int dragY;

    public HudEditorScreen(Screen parent, ConfigManager configManager) {
        super(Text.literal("BadCompany HUD Editor"));
        this.parent = parent;
        this.configManager = configManager;
    }

    @Override
    protected void init() {
        widgets.clear();
        addWidget("Watermark", 12, 12, 92, 18);
        addWidget("Coordinates", 12, height - 42, 122, 18);
        addWidget("Array List", width - 118, 42, 106, 58);
        addWidget("Status", width / 2 - 45, 12, 90, 18);
    }

    private void addWidget(String name, int x, int y, int width, int height) {
        ConfigManager.HudElementConfig saved = configManager.config().gui.hudElements.get(name);
        if (saved != null) widgets.add(new HudWidget(name, saved.x, saved.y, width, height, saved.enabled, saved.scale, saved.color));
        else widgets.add(new HudWidget(name, x, y, width, height, true, 1.0D, configManager.accentColor()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x77000000);
        context.fill(10, 10, width - 10, 35, ClickGuiScreen.alpha(configManager.backgroundColor(), 220));
        ClickGuiScreen.drawBorder(context, 10, 10, width - 20, 25, configManager.borderColor());
        context.drawText(textRenderer, "HUD Editor", 18, 18, configManager.textColor(), false);
        context.drawText(textRenderer, "drag elements • left click toggles • scroll scales • right click changes color • ESC saves", 92, 18, configManager.disabledModuleColor(), false);
        for (HudWidget widget : widgets) widget.render(context, mouseX, mouseY, configManager);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int button = click.button();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            HudWidget widget = widgets.get(i);
            if (!widget.contains(click.x(), click.y())) continue;
            if (button == 0) {
                widget.enabled = !widget.enabled;
                dragging = widget;
                dragX = (int) click.x() - widget.x;
                dragY = (int) click.y() - widget.y;
            } else if (button == 1) {
                widget.color = nextColor(widget.color);
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = null;
        saveHud();
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null) {
            dragging.x = Math.max(2, Math.min(width - dragging.scaledWidth() - 2, (int) click.x() - dragX));
            dragging.y = Math.max(2, Math.min(height - dragging.scaledHeight() - 2, (int) click.y() - dragY));
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HudWidget widget : widgets) {
            if (!widget.contains(mouseX, mouseY)) continue;
            widget.scale = Math.max(0.5D, Math.min(2.0D, widget.scale + (verticalAmount > 0 ? 0.1D : -0.1D)));
            saveHud();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        saveHud();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }

    private void saveHud() {
        for (HudWidget widget : widgets) {
            ConfigManager.HudElementConfig element = configManager.config().gui.hudElements.computeIfAbsent(widget.name, ignored -> new ConfigManager.HudElementConfig());
            element.x = widget.x;
            element.y = widget.y;
            element.enabled = widget.enabled;
            element.scale = widget.scale;
            element.color = widget.color;
        }
        configManager.saveSafely();
    }

    private static int nextColor(int current) {
        int[] colors = { 0xFF4CD964, 0xFF55FFFF, 0xFF7C4DFF, 0xFFFFCC4D, 0xFFFF66AA, 0xFFFFFFFF };
        for (int i = 0; i < colors.length; i++) if (colors[i] == current) return colors[(i + 1) % colors.length];
        return colors[0];
    }

    private static final class HudWidget {
        private final String name;
        private final int baseWidth;
        private final int baseHeight;
        private int x;
        private int y;
        private boolean enabled;
        private double scale;
        private int color;

        private HudWidget(String name, int x, int y, int baseWidth, int baseHeight, boolean enabled, double scale, int color) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.baseWidth = baseWidth;
            this.baseHeight = baseHeight;
            this.enabled = enabled;
            this.scale = scale;
            this.color = color;
        }

        private void render(DrawContext context, int mouseX, int mouseY, ConfigManager configManager) {
            int w = scaledWidth();
            int h = scaledHeight();
            boolean hovered = contains(mouseX, mouseY);
            context.fill(x, y, x + w, y + h, ClickGuiScreen.alpha(configManager.backgroundColor(), enabled ? 185 : 90));
            ClickGuiScreen.drawBorder(context, x, y, w, h, hovered ? color : configManager.borderColor());
            context.fill(x + 2, y + 2, x + 4, y + h - 2, enabled ? color : configManager.disabledModuleColor());
            String label = name + " x" + String.format(java.util.Locale.ROOT, "%.1f", scale);
            context.drawText(MinecraftClient.getInstance().textRenderer, CategoryPanel.truncate(label, w - 12), x + 8, y + Math.max(3, (h - 8) / 2), enabled ? configManager.textColor() : configManager.disabledModuleColor(), false);
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + scaledWidth() && mouseY >= y && mouseY <= y + scaledHeight();
        }

        private int scaledWidth() { return Math.max(32, (int) Math.round(baseWidth * scale)); }
        private int scaledHeight() { return Math.max(12, (int) Math.round(baseHeight * scale)); }
    }
}
