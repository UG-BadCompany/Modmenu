package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleStatus;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.ColorSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import com.badcompany.modmenu.settings.StringSetting;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

import java.util.Locale;

/**
 * Small, safe 1.21.x utility modules. These modules intentionally use vanilla
 * client state only; packet/exploit behavior lives in explicitly unsafe modules.
 */
public final class ModernUtilityModule extends Module {
    public enum Behavior {
        FULLBRIGHT,
        SPRINT,
        SNEAK,
        AUTO_WALK,
        COORDINATES_HUD,
        DIRECTION_HUD,
        FPS_HUD,
        MODULE_LIST_HUD,
        AUTO_RESPAWN,
        DEATH_COORDINATES,
        CHAT_LOGGER,
        FRIENDS,
        WAYPOINTS,
        NOTES,
        PANIC,
        VISUAL_SETTINGS,
        UNSAFE_STUB,
        PASSIVE
    }

    private final Behavior behavior;
    private final ColorSetting color;
    private final NumberSetting range;
    private final NumberSetting lineWidth;
    private final NumberSetting opacity;
    private final BooleanSetting showBoxes;
    private final BooleanSetting showTracers;
    private final BooleanSetting ignorePlayers;
    private final StringSetting includeList;
    private final StringSetting excludeList;
    private int ticks;
    private boolean deathLogged;

    public ModernUtilityModule(String name, String description, Category category, ModuleStatus status, Behavior behavior) {
        super(name, description, category, status);
        this.behavior = behavior;
        boolean visual = category == Category.RENDER || name.contains("ESP") || name.contains("Tracer") || name.contains("HUD") || name.contains("Highlight");
        this.color = visual ? addSetting(new ColorSetting("Module color", "Per-module ARGB render/HUD color.", defaultColor(category, status))) : null;
        this.range = visual || category == Category.PLAYER ? addSetting(new NumberSetting("Range", "Safe client-side range limit.", 64.0D, 4.0D, 256.0D)) : null;
        this.lineWidth = visual ? addSetting(new NumberSetting("Line width", "Preferred line width for render integrations.", 1.0D, 1.0D, 6.0D)) : null;
        this.opacity = visual ? addSetting(new NumberSetting("Alpha", "Preferred opacity for render integrations.", 150.0D, 16.0D, 255.0D)) : null;
        this.showBoxes = visual ? addSetting(new BooleanSetting("Boxes", "Enable box/outline rendering when a render hook is available.", true)) : null;
        this.showTracers = visual ? addSetting(new BooleanSetting("Tracers", "Enable tracer rendering when a render hook is available.", name.toLowerCase(Locale.ROOT).contains("tracer"))) : null;
        this.ignorePlayers = visual ? addSetting(new BooleanSetting("Ignore players", "Hide player targets for this visual module.", false)) : null;
        this.includeList = addSetting(new StringSetting("Include list", "Comma separated include filter or config section values.", ""));
        this.excludeList = addSetting(new StringSetting("Exclude list", "Comma separated exclude filter values.", ""));
    }

    @Override
    protected void onEnable() {
        ticks = 0;
        deathLogged = false;
        if (client.player != null && status() != ModuleStatus.UNSAFE_DISABLED) {
            client.player.sendMessage(Text.literal("[BadCompany] " + name() + " enabled (" + status().label() + ")."), false);
        }
    }

    @Override
    protected void onDisable() {
        if (behavior == Behavior.AUTO_WALK && client.options != null) client.options.forwardKey.setPressed(false);
        if (behavior == Behavior.SNEAK && client.options != null) client.options.sneakKey.setPressed(false);
    }

    @Override
    public void tick() {
        if (client.player == null) return;
        ticks++;
        switch (behavior) {
            case FULLBRIGHT -> client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, false, false, false));
            case SPRINT -> {
                if (client.options.forwardKey.isPressed()) client.player.setSprinting(true);
            }
            case SNEAK -> client.options.sneakKey.setPressed(true);
            case AUTO_WALK -> client.options.forwardKey.setPressed(true);
            case COORDINATES_HUD -> {
                if (ticks % 10 == 0) client.player.sendMessage(Text.literal(String.format(Locale.ROOT, "XYZ %.1f / %.1f / %.1f", client.player.getX(), client.player.getY(), client.player.getZ())), true);
            }
            case DIRECTION_HUD -> {
                if (ticks % 20 == 0) {
                    Direction facing = client.player.getHorizontalFacing();
                    client.player.sendMessage(Text.literal("Facing " + facing.asString().toUpperCase(Locale.ROOT)), true);
                }
            }
            case FPS_HUD -> {
                if (ticks % 20 == 0) client.player.sendMessage(Text.literal(client.getCurrentFps() + " FPS"), true);
            }
            case AUTO_RESPAWN -> {
                if (client.currentScreen instanceof DeathScreen || client.player.isDead()) client.player.requestRespawn();
            }
            case DEATH_COORDINATES -> {
                if (client.player.isDead() && !deathLogged) {
                    deathLogged = true;
                    client.player.sendMessage(Text.literal(String.format(Locale.ROOT, "[Death Coordinates] %.1f %.1f %.1f", client.player.getX(), client.player.getY(), client.player.getZ())), false);
                } else if (!client.player.isDead()) {
                    deathLogged = false;
                }
            }
            case MODULE_LIST_HUD -> {
                if (ticks % 60 == 0) client.player.sendMessage(Text.literal("Active modules are shown in the ClickGUI; HUD editor persistence is enabled via config."), true);
            }
            case CHAT_LOGGER, FRIENDS, WAYPOINTS, NOTES, VISUAL_SETTINGS, PANIC, PASSIVE, UNSAFE_STUB -> { }
        }
    }

    private static int defaultColor(Category category, ModuleStatus status) {
        if (status == ModuleStatus.UNSAFE_DISABLED) return 0xFFFF453A;
        return switch (category) {
            case COMBAT -> 0xFFFF5555;
            case MOVEMENT -> 0xFF55FF55;
            case RENDER -> 0xFF55FFFF;
            case PLAYER -> 0xFFFFFF55;
            case WORLD -> 0xFFFFAA55;
            case HUNTING -> 0xFFFF55FF;
            case MISC -> 0xFFBBBBBB;
            case EXPLOIT -> 0xFFFF453A;
        };
    }
}
