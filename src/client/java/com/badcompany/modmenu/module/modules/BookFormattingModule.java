package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleStatus;
import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.text.Text;

public final class BookFormattingModule extends Module {
    private final BooleanSetting showFormattingLegend = addSetting(new BooleanSetting(
            "Show legend",
            "Print modern formatting code reminders when a book editing screen is opened.",
            true
    ));

    private boolean wasEditingBook;

    public BookFormattingModule() {
        super("Book Formatting", "Provides safe client-side book formatting reminders for modern book edit screens.", Category.PLAYER, ModuleStatus.PARTIAL);
    }

    @Override
    protected void onDisable() {
        wasEditingBook = false;
    }

    @Override
    public void tick() {
        if (client.currentScreen == null || client.player == null) {
            wasEditingBook = false;
            return;
        }
        boolean editingBook = client.currentScreen.getClass().getSimpleName().contains("BookEditScreen");
        if (editingBook && !wasEditingBook && showFormattingLegend.get()) {
            client.player.sendMessage(Text.literal("[Book Formatting] Use § plus 0-9/a-f for colors, k-o for styles, and r to reset."), false);
        }
        wasEditingBook = editingBook;
    }
}
