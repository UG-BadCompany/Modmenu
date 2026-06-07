package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.StringSetting;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class PacketCancelerModule extends Module {
    private final StringSetting clientboundWatchList = addSetting(new StringSetting(
            "Clientbound watch list",
            "Comma separated packet class names to document for a future audited mixin hook.",
            ""
    ));
    private final StringSetting serverboundWatchList = addSetting(new StringSetting(
            "Serverbound watch list",
            "Comma separated packet class names to document for a future audited mixin hook.",
            ""
    ));
    private final BooleanSetting notifyChat = addSetting(new BooleanSetting(
            "Chat notices",
            "Explain that modern Packet Canceler is configuration-only and does not drop packets.",
            true
    ));

    private Set<String> clientbound = Set.of();
    private Set<String> serverbound = Set.of();

    public PacketCancelerModule() {
        super("Packet Canceler", "Stores packet-cancel filters safely without dropping modern protocol traffic.", Category.EXPLOIT);
    }

    @Override
    protected void onEnable() {
        clientbound = parseList(clientboundWatchList.get());
        serverbound = parseList(serverboundWatchList.get());
        if (notifyChat.get() && client.player != null) {
            client.player.sendMessage(Text.literal("[Packet Canceler] Safe mode active: " + clientbound.size() + " clientbound and " + serverbound.size() + " serverbound filters are saved but not dropped."), false);
        }
    }

    @Override
    public void tick() {
        clientbound = parseList(clientboundWatchList.get());
        serverbound = parseList(serverboundWatchList.get());
    }

    public Set<String> clientboundFilters() {
        return clientbound;
    }

    public Set<String> serverboundFilters() {
        return serverbound;
    }

    private static Set<String> parseList(String text) {
        if (text == null || text.isBlank()) return Set.of();
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
