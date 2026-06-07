package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.text.Text;

public final class PigPovModule extends Module {
    private final BooleanSetting forceFirstPerson = addSetting(new BooleanSetting(
            "Force first person",
            "Switch to first-person while riding a pig so tunnels are easier to navigate.",
            true
    ));
    private final BooleanSetting notifyChat = addSetting(new BooleanSetting(
            "Chat notices",
            "Show a local notice when Pig POV engages.",
            true
    ));

    private Perspective previousPerspective;
    private boolean activeOnPig;

    public PigPovModule() {
        super("Pig POV", "Modern pig-riding camera helper that preserves vanilla entity dimensions and rendering.", Category.RENDER);
    }

    @Override
    protected void onDisable() {
        restorePerspective();
        activeOnPig = false;
    }

    @Override
    public void tick() {
        if (client.player == null) {
            restorePerspective();
            activeOnPig = false;
            return;
        }

        boolean ridingPig = client.player.getVehicle() instanceof PigEntity;
        if (ridingPig && !activeOnPig) {
            activeOnPig = true;
            previousPerspective = client.options.getPerspective();
            if (forceFirstPerson.get()) client.options.setPerspective(Perspective.FIRST_PERSON);
            if (notifyChat.get()) client.player.sendMessage(Text.literal("[Pig POV] Pig-riding camera helper active."), false);
        } else if (!ridingPig && activeOnPig) {
            restorePerspective();
            activeOnPig = false;
        }
    }

    private void restorePerspective() {
        if (previousPerspective != null && client.options != null) {
            client.options.setPerspective(previousPerspective);
        }
        previousPerspective = null;
    }
}
