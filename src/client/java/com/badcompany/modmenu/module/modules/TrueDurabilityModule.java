package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.BadCompanyClient;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleStatus;
import com.badcompany.modmenu.settings.BooleanSetting;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public final class TrueDurabilityModule extends Module {
    private final BooleanSetting showMaxDamage = addSetting(new BooleanSetting(
            "Show max damage",
            "Include the item's maximum damage value in durability tooltips.",
            true
    ));
    private final BooleanSetting flagUnbreakable = addSetting(new BooleanSetting(
            "Flag unbreakable",
            "Add a red tooltip marker to unbreakable damageable items.",
            true
    ));
    private boolean callbackRegistered;

    public TrueDurabilityModule() {
        super("True Durability", "Shows exact durability values in modern tooltips.", Category.RENDER, ModuleStatus.WORKING, true);
        registerTooltipCallback();
    }

    @Override
    protected void onEnable() {
        validateRuntimeState();
        BadCompanyClient.LOGGER.info("True Durability enabled: exact durability tooltips are active");
    }

    private void registerTooltipCallback() {
        ItemTooltipCallback.EVENT.register(this::appendDurabilityTooltip);
        callbackRegistered = true;
    }

    private void appendDurabilityTooltip(ItemStack stack, net.minecraft.item.Item.TooltipContext context, net.minecraft.item.tooltip.TooltipType type, List<Text> lines) {
        if (!enabled() || stack == null || stack.isEmpty() || !stack.isDamageable()) return;

        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) return;

        boolean unbreakable = stack.contains(DataComponentTypes.UNBREAKABLE);
        if (unbreakable) {
            appendUnbreakableTooltip(lines);
            return;
        }

        int damage = stack.getDamage();
        long remaining = (long) maxDamage - damage;
        lines.add(Text.empty());
        lines.add(formatDurabilityLine(remaining, maxDamage, damage));
    }

    private void appendUnbreakableTooltip(List<Text> lines) {
        if (!flagUnbreakable.get()) return;
        lines.add(Text.literal("Unbreakable item").formatted(Formatting.RED));
    }

    private Text formatDurabilityLine(long remaining, int maxDamage, int damage) {
        Formatting color = durabilityColor(damage, maxDamage);
        String suffix = showMaxDamage.get() ? " [Max: " + maxDamage + "]" : "";
        return Text.literal("Durability: " + remaining + suffix).formatted(color);
    }

    private Formatting durabilityColor(int damage, int maxDamage) {
        if (damage < 0) return Formatting.DARK_PURPLE;
        if (damage > maxDamage) return Formatting.DARK_RED;
        return Formatting.BLUE;
    }

    private void validateRuntimeState() {
        if (!callbackRegistered) {
            throw new IllegalStateException("True Durability tooltip callback was not registered");
        }
        if (showMaxDamage.get() == null || flagUnbreakable.get() == null) {
            throw new IllegalStateException("True Durability settings were not initialized");
        }
    }
}
