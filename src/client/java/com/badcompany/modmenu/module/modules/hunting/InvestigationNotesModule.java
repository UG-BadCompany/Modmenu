package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.StringSetting;

public final class InvestigationNotesModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final StringSetting note = addSetting(new StringSetting("Note text", "Coordinate note saved at your current position when Save note is enabled.", "Found old sign here. Possible stash nearby. Need to revisit."));
    private final BooleanSetting saveNote = addSetting(new BooleanSetting("Save note", "Toggle on to save the configured note at your current coordinate, then automatically turns off.", false));
    public InvestigationNotesModule() { super("Investigation Notes", "Stores searchable notes at exact coordinates for archaeology and revisit planning.", Category.HUNTING); }
    @Override public void tick() {
        database.tickAutosave();
        if (!saveNote.get() || client.player == null) return;
        database.recordNote(client.player.getBlockPos(), HuntDatabase.dimension(), note.get());
        saveNote.set(false);
    }
}
