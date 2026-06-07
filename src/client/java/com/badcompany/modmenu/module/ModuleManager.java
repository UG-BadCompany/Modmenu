package com.badcompany.modmenu.module;

import com.badcompany.modmenu.config.ConfigManager;
import com.badcompany.modmenu.module.modules.AdvancedSearchModule;
import com.badcompany.modmenu.module.modules.PlaceholderModule;
import com.badcompany.modmenu.module.modules.TrueDurabilityModule;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Category, List<Module>> byCategory = new EnumMap<>(Category.class);
    private ConfigManager configManager;

    public void attachConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void registerDefaults() {
        register(new TrueDurabilityModule());
        register(new AdvancedSearchModule());
        register(new PlaceholderModule("Book Formatting", "Modern book text formatting helpers.", Category.PLAYER, "Book edit screen integration is staged for a focused mixin."));
        register(new PlaceholderModule("Silent Close", "Closes selected screens without sending legacy close packets.", Category.EXPLOIT, "Packet behavior changed in 1.21; implementation is intentionally safe."));
        register(new PlaceholderModule("Packet Canceler", "Selective client packet cancellation.", Category.EXPLOIT, "Requires packet-specific Fabric mixins before enabling live cancellation."));
        register(new PlaceholderModule("Portal Invulnerability", "Legacy portal damage exploit recreation.", Category.EXPLOIT, "Known legacy exploit is patched on modern servers."));
        register(new PlaceholderModule("Pig POV", "Camera utilities inspired by the original Pig POV feature.", Category.RENDER, "Camera transform hooks are staged for future mixin work."));
        register(new PlaceholderModule("Entity Trace", "Draws tracers to selected entities.", Category.RENDER, "World rendering implementation is stubbed to preserve stability."));
        register(new PlaceholderModule("Stalker", "Tracks selected players client-side.", Category.PLAYER, "Player tracking state exists; network/GUI selectors are pending."));
        register(new PlaceholderModule("Undead", "Legacy death-state utility behavior.", Category.PLAYER, "Modern respawn flow differs and needs safe server checks."));
        register(new PlaceholderModule("Bowbomb", "Legacy projectile exploit placeholder.", Category.EXPLOIT, "Exploit packet flood is not implemented to avoid unsafe behavior."));
        register(new PlaceholderModule("PumpkinAura", "Automatically places pumpkins when rules allow.", Category.WORLD, "Placement automation needs anti-desync logic before activation."));
        modules.sort(Comparator.comparing(Module::name));
        byCategory.values().forEach(list -> list.sort(Comparator.comparing(Module::name)));
    }

    public void register(Module module) {
        modules.add(module);
        byCategory.computeIfAbsent(module.category(), ignored -> new ArrayList<>()).add(module);
    }

    public List<Module> modules() { return Collections.unmodifiableList(modules); }
    public List<Module> modules(Category category) { return Collections.unmodifiableList(byCategory.getOrDefault(category, List.of())); }

    public Optional<Module> find(String name) {
        String normalized = normalize(name);
        return modules.stream().filter(module -> normalize(module.name()).equals(normalized)).findFirst();
    }

    public void tick() {
        for (Module module : modules) {
            if (module.enabled()) module.tick();
        }
    }

    public void handleKey(InputUtil.Key key) {
        if (key == null || key == InputUtil.UNKNOWN_KEY) return;
        for (Module module : modules) {
            if (module.keybind().equals(key)) {
                module.toggle();
                saveSoon();
            }
        }
    }

    public void saveSoon() {
        if (configManager != null) configManager.saveSafely();
    }

    private static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).replace(" ", "");
    }
}
