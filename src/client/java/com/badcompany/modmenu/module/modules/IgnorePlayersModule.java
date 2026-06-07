package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.StringSetting;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class IgnorePlayersModule extends Module {
    private final StringSetting ignoredPlayers = addSetting(new StringSetting(
            "Ignored players",
            "Comma separated player names hidden from client chat while this module is enabled.",
            ""
    ));
    private final BooleanSetting hideGameMessages = addSetting(new BooleanSetting(
            "Hide matching game messages",
            "Also hide unsigned server/game messages whose text contains an ignored player name.",
            false
    ));

    public IgnorePlayersModule() {
        super("Ignore Players", "Client-side ignore list for hiding chat from selected players.", Category.MISC);
        ClientReceiveMessageEvents.ALLOW_CHAT.register(this::allowChatMessage);
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::allowGameMessage);
    }

    private boolean allowChatMessage(Text message, net.minecraft.network.message.SignedMessage signedMessage, GameProfile sender, net.minecraft.network.message.MessageType.Parameters params, Instant receptionTimestamp) {
        if (!enabled() || sender == null) return true;
        return !ignoredNames().contains(normalize(sender.name()));
    }

    private boolean allowGameMessage(Text message, boolean overlay) {
        if (!enabled() || !hideGameMessages.get()) return true;
        String text = normalize(message == null ? "" : message.getString());
        return ignoredNames().stream().noneMatch(text::contains);
    }

    private Set<String> ignoredNames() {
        return Arrays.stream(ignoredPlayers.get().split(","))
                .map(IgnorePlayersModule::normalize)
                .filter(name -> !name.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
