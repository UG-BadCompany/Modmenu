package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.util.math.BlockPos;

public final class AutomaticEvidenceCaptureModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final BooleanSetting capturePlayers = addSetting(new BooleanSetting("Capture players", "Record evidence rows for newly observed players.", true));
    private final BooleanSetting captureStructures = addSetting(new BooleanSetting("Capture structures", "Record evidence rows for portals, stashes, bases, and signs.", true));
    private int playerCount;
    private int structureCount;
    public AutomaticEvidenceCaptureModule() { super("Automatic Evidence Capture", "Captures coordinates and chunk context when players, portals, stashes, bases, and signs are discovered.", Category.HUNTING); }
    @Override public void tick() {
        database.tickAutosave();
        HuntDatabase.Data data = database.data();
        if (capturePlayers.get() && data.players.size() > playerCount) {
            data.players.subList(playerCount, data.players.size()).forEach(row -> database.recordEvidence(new BlockPos((int) row.x(), (int) row.y(), (int) row.z()), row.dimension(), "player_found", row.name()));
        }
        int newStructureCount = data.portals.size() + data.stashes.size() + data.bases.size() + data.signs.size();
        if (captureStructures.get() && newStructureCount > structureCount && client.player != null) database.recordEvidence(client.player.getBlockPos(), HuntDatabase.dimension(), "structure_found", "Portal/stash/base/sign database grew");
        playerCount = data.players.size();
        structureCount = newStructureCount;
    }
}
