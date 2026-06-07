package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.util.math.BlockPos;

public final class AiBasePredictorModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting interval = addSetting(new NumberSetting("Prediction interval", "Ticks between base prediction passes.", 900.0D, 100.0D, 3600.0D));
    private final NumberSetting radius = addSetting(new NumberSetting("Search radius", "Clue radius for generated likely-base predictions.", 2500.0D, 512.0D, 10000.0D));
    private int cooldown;
    public AiBasePredictorModule() { super("AI Base Predictor", "Correlates players, trails, portals, signs, chunk changes, and storage clusters into likely active base predictions.", Category.HUNTING); }
    @Override public void tick() {
        database.tickAutosave();
        if (cooldown-- > 0) return;
        cooldown = (int) Math.round(interval.get());
        HuntDatabase.Data data = database.data();
        data.players.stream().limit(40).forEach(anchor -> {
            long portals = data.portals.stream().filter(row -> close(row.x(), row.z(), anchor.x(), anchor.z())).count();
            long trails = data.trails.stream().filter(row -> close(row.x(), row.z(), anchor.x(), anchor.z())).count();
            long signs = data.signs.stream().filter(row -> close(row.x(), row.z(), anchor.x(), anchor.z())).count();
            long changes = data.chunkChanges.stream().filter(row -> close(row.x(), row.z(), anchor.x(), anchor.z())).count();
            long storage = data.stashes.stream().filter(row -> close(row.x(), row.z(), anchor.x(), anchor.z())).count();
            int confidence = (int) Math.min(100, 20 + portals * 12 + trails / 2 + signs * 8 + changes * 10 + storage * 18);
            if (confidence >= 65) database.recordBase(new BlockPos((int) anchor.x(), (int) anchor.y(), (int) anchor.z()), anchor.dimension(), confidence, (int) Math.round(radius.get()), "Likely Active Base: player route plus portal/trail/sign/change/storage correlation");
        });
    }
    private boolean close(double ax, double az, double bx, double bz) { double dx = ax - bx; double dz = az - bz; double r = radius.get(); return dx * dx + dz * dz <= r * r; }
}
