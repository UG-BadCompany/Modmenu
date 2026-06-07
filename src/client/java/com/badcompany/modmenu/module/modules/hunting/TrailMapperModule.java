package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.StringSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class TrailMapperModule extends AbstractBlockScanModule {
    private final StringSetting blockList = addSetting(new StringSetting("Trail blocks", "Comma separated block id fragments recorded as artificial trail clues.", "obsidian,cobblestone,netherrack,rail,torch,sign,button,ladder,scaffolding,glass,dirt,path"));
    private final BooleanSetting breadcrumbMarkers = addSetting(new BooleanSetting("Breadcrumb markers", "Expose recorded trail points for breadcrumb rendering integrations.", true));
    private final BooleanSetting tracerLines = addSetting(new BooleanSetting("Tracer lines", "Expose trail points for optional tracer-line rendering integrations.", false));
    private final BooleanSetting minimapOverlay = addSetting(new BooleanSetting("Minimap overlay", "Keep trail records tagged for minimap overlay integrations.", false));

    public TrailMapperModule() {
        super("Trail Mapper", "Discovers and records artificial player trails over many sessions.", 48.0D, 40.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (matches(blockId)) database.recordTrail(pos, dimension, blockId);
    }

    public boolean breadcrumbMarkers() { return breadcrumbMarkers.get(); }
    public boolean tracerLines() { return tracerLines.get(); }
    public boolean minimapOverlay() { return minimapOverlay.get(); }

    private boolean matches(String blockId) {
        Set<String> fragments = Arrays.stream(blockList.get().split(","))
                .map(text -> text.trim().toLowerCase(Locale.ROOT))
                .filter(text -> !text.isBlank())
                .collect(Collectors.toSet());
        String id = blockId.toLowerCase(Locale.ROOT);
        return fragments.stream().anyMatch(id::contains);
    }
}
