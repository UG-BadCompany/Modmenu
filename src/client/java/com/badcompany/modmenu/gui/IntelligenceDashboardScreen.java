package com.badcompany.modmenu.gui;

import com.badcompany.modmenu.hunting.HuntDatabase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class IntelligenceDashboardScreen extends Screen {
    private final Screen parent;
    private final HuntDatabase database = HuntDatabase.get();

    public IntelligenceDashboardScreen(Screen parent) {
        super(Text.literal("Intelligence Dashboard"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xCC050509);
        context.fill(18, 18, width - 18, height - 18, 0xEE17171E);
        context.fill(18, 42, width - 18, 44, 0xFF7C4DFF);
        context.drawTextWithShadow(textRenderer, "BadCompany Intelligence Dashboard", 28, 28, 0xFFFFFFFF);
        HuntDatabase.DashboardSummary summary = database.dashboardSummary();
        int y = 58;
        y = line(context, "Active investigations: " + summary.activeInvestigations(), y, 0xFFFFD166);
        y = line(context, "Recent discoveries: trails=" + summary.trails() + ", portals=" + summary.portals() + ", signs=" + summary.signs() + ", books=" + summary.books(), y, 0xFFECECEC);
        y = line(context, "Player sightings: " + summary.players(), y, 0xFF8CE8FF);
        y = line(context, "Storage/stash leads: " + summary.stashes(), y, 0xFFB8FF8C);
        y = line(context, "Base predictions: " + summary.bases() + " (top confidence " + summary.topConfidence() + "%)", y, 0xFFFF8C8C);
        y = line(context, "Chunk changes: " + summary.chunkChanges(), y, 0xFFD8B8FF);
        y += 8;
        y = line(context, "Top prediction:", y, 0xFFFFFFFF);
        line(context, summary.topPrediction(), y, 0xFFECECEC);
        context.drawTextWithShadow(textRenderer, "Esc: back to ClickGUI", 28, height - 34, 0xFFAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    private int line(DrawContext context, String text, int y, int color) {
        context.drawTextWithShadow(textRenderer, text, 28, y, color);
        return y + 14;
    }

    @Override
    public void close() { MinecraftClient.getInstance().setScreen(parent); }

    @Override
    public boolean shouldPause() { return false; }
}
