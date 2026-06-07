package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.NumberSetting;

public final class RegionScannerModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final NumberSetting interval = addSetting(new NumberSetting("Region interval", "Ticks between 512x512 region scoring passes.", 400.0D, 100.0D, 2400.0D));
    private int cooldown;
    public RegionScannerModule() { super("Region Scanner", "Scores every visited 512x512 region for portals, players, trails, storage, and base probability.", Category.HUNTING); }
    @Override public void tick() {
        database.tickAutosave();
        if (client.player == null || cooldown-- > 0) return;
        cooldown = (int) Math.round(interval.get());
        String dimension = HuntDatabase.dimension();
        int rx = Math.floorDiv(client.player.getBlockX(), 512);
        int rz = Math.floorDiv(client.player.getBlockZ(), 512);
        HuntDatabase.Data data = database.data();
        int portals = (int) data.portals.stream().filter(row -> row.dimension().equals(dimension) && region(row.x()) == rx && region(row.z()) == rz).count();
        int players = (int) data.players.stream().filter(row -> row.dimension().equals(dimension) && region((int) row.x()) == rx && region((int) row.z()) == rz).count();
        int trails = (int) data.trails.stream().filter(row -> row.dimension().equals(dimension) && region(row.x()) == rx && region(row.z()) == rz).count();
        int storage = (int) data.stashes.stream().filter(row -> row.dimension().equals(dimension) && region(row.x()) == rx && region(row.z()) == rz).count();
        int probability = Math.min(100, portals * 10 + players * 5 + trails / 4 + storage * 20);
        database.recordRegion(dimension, rx, rz, portals, players, trails, storage, probability);
    }
    private static int region(int coordinate) { return Math.floorDiv(coordinate, 512); }
}
