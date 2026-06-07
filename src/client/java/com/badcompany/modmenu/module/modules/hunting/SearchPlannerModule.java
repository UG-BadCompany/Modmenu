package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.util.math.BlockPos;

public final class SearchPlannerModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting interval = addSetting(new NumberSetting("Planner interval", "Ticks between next-search recommendations.", 1200.0D, 200.0D, 6000.0D));
    private int cooldown;
    public SearchPlannerModule() { super("Search Planner", "Suggests where to search next based on anomaly density, recent player activity, and trail intersections.", Category.HUNTING); }
    @Override public void tick() {
        database.tickAutosave();
        if (client.player == null || cooldown-- > 0) return;
        cooldown = (int) Math.round(interval.get());
        database.data().bases.stream().max(java.util.Comparator.comparingInt(HuntDatabase.BaseRecord::confidence)).ifPresent(base ->
                database.recordExpedition(new BlockPos(base.x(), base.y(), base.z()), base.dimension(), "Recommended Search: investigate high-confidence prediction (" + base.confidence() + "%) radius " + base.radius() + " blocks", true));
    }
}
