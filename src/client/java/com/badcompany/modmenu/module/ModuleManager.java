package com.badcompany.modmenu.module;

import com.badcompany.modmenu.config.ConfigManager;
import com.badcompany.modmenu.module.modules.AdvancedSearchModule;
import com.badcompany.modmenu.module.modules.BookFormattingModule;
import com.badcompany.modmenu.module.modules.BowbombModule;
import com.badcompany.modmenu.module.modules.EntityTraceModule;
import com.badcompany.modmenu.module.modules.PacketCancelerModule;
import com.badcompany.modmenu.module.modules.PigPovModule;
import com.badcompany.modmenu.module.modules.PortalInvulnerabilityModule;
import com.badcompany.modmenu.module.modules.PumpkinAuraModule;
import com.badcompany.modmenu.module.modules.SilentCloseModule;
import com.badcompany.modmenu.module.modules.StalkerModule;
import com.badcompany.modmenu.module.modules.TrueDurabilityModule;
import com.badcompany.modmenu.module.modules.UndeadModule;
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
        modules.clear();
        byCategory.clear();

        register(new TrueDurabilityModule());
        register(new AdvancedSearchModule());
        register(new BookFormattingModule());
        register(new SilentCloseModule());
        register(new PacketCancelerModule());
        register(new PortalInvulnerabilityModule());
        register(new PigPovModule());
        register(new EntityTraceModule());
        register(new StalkerModule());
        register(new UndeadModule());
        register(new BowbombModule());
        register(new PumpkinAuraModule());
        modules.sort(Comparator.comparing(Module::name));
        byCategory.values().forEach(list -> list.sort(Comparator.comparing(Module::name)));
        validateRegistration();
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

    private void validateRegistration() {
        for (Category category : Category.values()) {
            byCategory.computeIfAbsent(category, ignored -> new ArrayList<>());
        }
        long uniqueNames = modules.stream().map(module -> normalize(module.name())).distinct().count();
        if (uniqueNames != modules.size()) {
            throw new IllegalStateException("Duplicate BadCompany module names are not allowed");
        }
    }

    private static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).replace(" ", "");
    }
}
