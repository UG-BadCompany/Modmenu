package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleStatus;
import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;

public final class SilentCloseModule extends Module {
    private final BooleanSetting notifyChat = addSetting(new BooleanSetting(
            "Chat notices",
            "Report container open/close transitions without suppressing vanilla close packets.",
            true
    ));
    private final BooleanSetting rememberLastContainer = addSetting(new BooleanSetting(
            "Remember last container",
            "Keep local details for the last handled screen so accidental closes are visible to the player.",
            true
    ));

    private boolean wasInContainer;
    private String lastContainerTitle = "";
    private int lastSyncId = -1;

    public SilentCloseModule() {
        super("Silent Close", "Safely tracks handled-screen closes; modern clients still send vanilla close packets.", Category.EXPLOIT, ModuleStatus.PARTIAL);
    }

    @Override
    protected void onDisable() {
        wasInContainer = false;
        lastContainerTitle = "";
        lastSyncId = -1;
    }

    @Override
    public void tick() {
        if (client.player == null) {
            wasInContainer = false;
            return;
        }

        if (client.currentScreen instanceof HandledScreen<?> handledScreen) {
            wasInContainer = true;
            if (rememberLastContainer.get()) {
                lastContainerTitle = handledScreen.getTitle().getString();
                lastSyncId = handledScreen.getScreenHandler().syncId;
            }
            return;
        }

        if (wasInContainer) {
            wasInContainer = false;
            if (notifyChat.get() && rememberLastContainer.get()) {
                client.player.sendMessage(Text.literal("[Silent Close] Closed " + describeLastContainer() + ". Vanilla close handling was preserved."), false);
            }
        }
    }

    public String describeLastContainer() {
        if (lastSyncId < 0 || lastContainerTitle.isBlank()) return "the last container";
        return "'" + lastContainerTitle + "' (sync " + lastSyncId + ")";
    }
}
