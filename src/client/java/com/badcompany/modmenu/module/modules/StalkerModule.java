package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class StalkerModule extends Module {
    private final BooleanSetting reportJoins = addSetting(new BooleanSetting(
            "Report joins",
            "Notify when players appear in the client tab-list cache.",
            true
    ));
    private final BooleanSetting reportLeaves = addSetting(new BooleanSetting(
            "Report leaves",
            "Notify when players disappear from the client tab-list cache.",
            true
    ));
    private final BooleanSetting reportGameModes = addSetting(new BooleanSetting(
            "Report gamemodes",
            "Notify when a tab-list player changes game mode.",
            true
    ));

    private final Map<UUID, PlayerSnapshot> players = new HashMap<>();
    private int warmupTicks;

    public StalkerModule() {
        super("Stalker", "Tracks tab-list joins, leaves, and game mode changes using modern client state.", Category.PLAYER);
    }

    @Override
    protected void onEnable() {
        players.clear();
        warmupTicks = 2;
    }

    @Override
    protected void onDisable() {
        players.clear();
    }

    @Override
    public void tick() {
        if (client.getNetworkHandler() == null || client.player == null) {
            players.clear();
            return;
        }

        Set<UUID> seen = new HashSet<>();
        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            UUID uuid = entry.getProfile().id();
            seen.add(uuid);
            PlayerSnapshot current = PlayerSnapshot.from(entry);
            PlayerSnapshot previous = players.put(uuid, current);
            if (warmupTicks > 0) continue;
            if (previous == null) {
                if (reportJoins.get()) notify(current.name + " joined the tab list");
            } else if (reportGameModes.get() && !previous.gameMode.equals(current.gameMode)) {
                notify(current.name + " changed game mode from " + previous.gameMode + " to " + current.gameMode);
            }
        }

        if (warmupTicks <= 0 && reportLeaves.get()) {
            for (Map.Entry<UUID, PlayerSnapshot> entry : players.entrySet()) {
                if (!seen.contains(entry.getKey())) notify(entry.getValue().name + " left the tab list");
            }
        }
        players.keySet().removeIf(uuid -> !seen.contains(uuid));
        if (warmupTicks > 0) warmupTicks--;
    }

    private void notify(String message) {
        if (client.player == null) return;
        client.player.sendMessage(Text.literal("[Stalker] " + message), false);
    }

    private record PlayerSnapshot(String name, String gameMode) {
        static PlayerSnapshot from(PlayerListEntry entry) {
            String mode = entry.getGameMode() == null ? "unknown" : entry.getGameMode().asString();
            return new PlayerSnapshot(entry.getProfile().name(), mode);
        }
    }
}
