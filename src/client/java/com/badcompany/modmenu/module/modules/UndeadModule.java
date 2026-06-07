package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.Text;

public final class UndeadModule extends Module {
    private final BooleanSetting closeDeathScreen = addSetting(new BooleanSetting(
            "Close death screen",
            "Dismiss the modern DeathScreen client-side so vanilla respawn handling stays under user control.",
            true
    ));
    private final BooleanSetting notifyChat = addSetting(new BooleanSetting(
            "Chat notices",
            "Show a local reminder when the death screen is dismissed.",
            true
    ));

    private boolean notifiedForDeath;

    public UndeadModule() {
        super("Undead", "Safely dismisses the client death screen without sending respawn packets automatically.", Category.PLAYER);
    }

    @Override
    protected void onDisable() {
        notifiedForDeath = false;
    }

    @Override
    public void tick() {
        if (client.player == null) {
            notifiedForDeath = false;
            return;
        }
        if (!client.player.isDead()) {
            notifiedForDeath = false;
            return;
        }
        if (closeDeathScreen.get() && client.currentScreen instanceof DeathScreen) {
            client.setScreen(null);
            if (!notifiedForDeath && notifyChat.get()) {
                client.player.sendMessage(Text.literal("[Undead] Death screen closed client-side. Use the vanilla respawn action when ready."), false);
            }
            notifiedForDeath = true;
        }
    }
}
