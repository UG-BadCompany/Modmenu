package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleStatus;
import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public final class BowbombModule extends Module {
    private final BooleanSetting notifyUnsafeRelease = addSetting(new BooleanSetting(
            "Release notices",
            "Notify when a charged bow is released instead of sending unsafe packet floods.",
            true
    ));
    private final BooleanSetting requireFullCharge = addSetting(new BooleanSetting(
            "Full charge only",
            "Only report bow releases after the vanilla full-charge threshold.",
            true
    ));

    private boolean wasUsingBow;
    private int lastUseTicks;

    public BowbombModule() {
        super("Bowbomb", "Safe bow release monitor; unsafe packet burst behavior is intentionally not sent.", Category.EXPLOIT, ModuleStatus.UNSAFE_DISABLED);
    }

    @Override
    protected void onDisable() {
        wasUsingBow = false;
        lastUseTicks = 0;
    }

    @Override
    public void tick() {
        if (client.player == null) {
            wasUsingBow = false;
            return;
        }

        boolean usingBow = client.player.isUsingItem() && client.player.getActiveItem().isOf(Items.BOW);
        if (usingBow) {
            wasUsingBow = true;
            lastUseTicks = client.player.getItemUseTime();
            return;
        }

        if (wasUsingBow) {
            wasUsingBow = false;
            if (notifyUnsafeRelease.get() && (!requireFullCharge.get() || lastUseTicks >= 20)) {
                client.player.sendMessage(Text.literal("[Bowbomb] Bow released normally. Unsafe packet flooding is disabled in the Fabric port."), false);
            }
            lastUseTicks = 0;
        }
    }
}
