package com.badcompany.modmenu.module;

import com.badcompany.modmenu.config.ConfigManager;
import com.badcompany.modmenu.module.modules.AdvancedSearchModule;
import com.badcompany.modmenu.module.modules.BookFormattingModule;
import com.badcompany.modmenu.module.modules.BowbombModule;
import com.badcompany.modmenu.module.modules.ClientCommandsModule;
import com.badcompany.modmenu.module.modules.EntityTraceModule;
import com.badcompany.modmenu.module.modules.IgnorePlayersModule;
import com.badcompany.modmenu.module.modules.ModernUtilityModule;
import com.badcompany.modmenu.module.modules.PacketCancelerModule;
import com.badcompany.modmenu.module.modules.PigPovModule;
import com.badcompany.modmenu.module.modules.PortalInvulnerabilityModule;
import com.badcompany.modmenu.module.modules.PumpkinAuraModule;
import com.badcompany.modmenu.module.modules.SilentCloseModule;
import com.badcompany.modmenu.module.modules.StalkerModule;
import com.badcompany.modmenu.module.modules.TrueDurabilityModule;
import com.badcompany.modmenu.module.modules.UndeadModule;
import com.badcompany.modmenu.module.modules.hunting.ArtificialBlockDetectorModule;
import com.badcompany.modmenu.module.modules.hunting.WorldHistoryViewerModule;
import com.badcompany.modmenu.module.modules.hunting.WorldHeatmapModule;
import com.badcompany.modmenu.module.modules.hunting.SharedHuntFilesModule;
import com.badcompany.modmenu.module.modules.hunting.SearchPlannerModule;
import com.badcompany.modmenu.module.modules.hunting.SafeLogoutFinderModule;
import com.badcompany.modmenu.module.modules.hunting.RegionScannerModule;
import com.badcompany.modmenu.module.modules.hunting.InvestigationNotesModule;
import com.badcompany.modmenu.module.modules.hunting.HighwayAiModule;
import com.badcompany.modmenu.module.modules.hunting.ExplorationStatisticsModule;
import com.badcompany.modmenu.module.modules.hunting.ExpeditionSystemModule;
import com.badcompany.modmenu.module.modules.hunting.ElytraFlightLoggerModule;
import com.badcompany.modmenu.module.modules.hunting.BaseArchaeologyModule;
import com.badcompany.modmenu.module.modules.hunting.AutomaticEvidenceCaptureModule;
import com.badcompany.modmenu.module.modules.hunting.AiBasePredictorModule;
import com.badcompany.modmenu.module.modules.hunting.BookArchiveModule;
import com.badcompany.modmenu.module.modules.hunting.ChunkChangeTrackerModule;
import com.badcompany.modmenu.module.modules.hunting.HiddenEntranceDetectorModule;
import com.badcompany.modmenu.module.modules.hunting.HighwayDivergenceDetectorModule;
import com.badcompany.modmenu.module.modules.hunting.HuntDatabaseModule;
import com.badcompany.modmenu.module.modules.hunting.LongDistanceEspModule;
import com.badcompany.modmenu.module.modules.hunting.MultiSessionIntelligenceEngineModule;
import com.badcompany.modmenu.module.modules.hunting.PlayerActivityHeatmapModule;
import com.badcompany.modmenu.module.modules.hunting.PortalDatabaseModule;
import com.badcompany.modmenu.module.modules.hunting.SignDatabaseModule;
import com.badcompany.modmenu.module.modules.hunting.StashProbabilityScannerModule;
import com.badcompany.modmenu.module.modules.hunting.TerrainAnomalyFinderModule;
import com.badcompany.modmenu.module.modules.hunting.TorchClusterFinderModule;
import com.badcompany.modmenu.module.modules.hunting.TrailMapperModule;
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
        register(new ClientCommandsModule());
        register(new IgnorePlayersModule());
        register(new SilentCloseModule());
        register(new PacketCancelerModule());
        register(new PortalInvulnerabilityModule());
        register(new PigPovModule());
        register(new EntityTraceModule());
        register(new StalkerModule());
        register(new UndeadModule());
        register(new BowbombModule());
        register(new PumpkinAuraModule());

        registerModernModules();

        register(new TrailMapperModule());
        register(new StashProbabilityScannerModule());
        register(new PlayerActivityHeatmapModule());
        register(new WorldHeatmapModule());
        register(new ChunkChangeTrackerModule());
        register(new BaseArchaeologyModule());
        register(new HighwayAiModule());
        register(new HighwayDivergenceDetectorModule());
        register(new PortalDatabaseModule());
        register(new ArtificialBlockDetectorModule());
        register(new TorchClusterFinderModule());
        register(new HiddenEntranceDetectorModule());
        register(new SignDatabaseModule());
        register(new BookArchiveModule());
        register(new AutomaticEvidenceCaptureModule());
        register(new ExpeditionSystemModule());
        register(new RegionScannerModule());
        register(new HuntDatabaseModule());
        register(new LongDistanceEspModule());
        register(new TerrainAnomalyFinderModule());
        register(new AiBasePredictorModule());
        register(new SearchPlannerModule());
        register(new SharedHuntFilesModule());
        register(new InvestigationNotesModule());
        register(new SafeLogoutFinderModule());
        register(new ElytraFlightLoggerModule());
        register(new WorldHistoryViewerModule());
        register(new ExplorationStatisticsModule());
        register(new MultiSessionIntelligenceEngineModule());
        modules.sort(Comparator.comparing(Module::name));
        byCategory.values().forEach(list -> list.sort(Comparator.comparing(Module::name)));
        validateRegistration();
    }

    private void registerModernModules() {
        register(new ModernUtilityModule("Fullbright", "Applies a client-side night-vision visual aid while enabled.", Category.RENDER, ModuleStatus.WORKING, ModernUtilityModule.Behavior.FULLBRIGHT));
        register(new ModernUtilityModule("No Fog", "Registered as a render preference; disabled until a safe 1.21.x fog render hook is added.", Category.RENDER, ModuleStatus.PLACEHOLDER, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("ESP", "Configures ESP colors/range; full world-render boxes require a render backend.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Tracers", "Configures tracer colors/range/line width for safe render integrations.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Storage ESP", "Storage highlight configuration for loaded block-entity render integrations.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Item ESP", "Item highlight configuration for loaded entity render integrations.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Nametags", "Modern nametag customization shell; no unsafe name spoofing is attempted.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Block Highlight", "Per-module block highlight color/range settings for loaded-world targets.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Search", "Basic search preset entry point; use Advanced Search for block/state filters.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Freecam", "Disabled: safe camera/entity decoupling needs mixin-backed movement isolation on 1.21.x.", Category.RENDER, ModuleStatus.UNSAFE_DISABLED, ModernUtilityModule.Behavior.UNSAFE_STUB));
        register(new ModernUtilityModule("Coordinates HUD", "Shows current coordinates in the action bar.", Category.RENDER, ModuleStatus.WORKING, ModernUtilityModule.Behavior.COORDINATES_HUD));
        register(new ModernUtilityModule("Direction HUD", "Shows current horizontal facing in the action bar.", Category.RENDER, ModuleStatus.WORKING, ModernUtilityModule.Behavior.DIRECTION_HUD));
        register(new ModernUtilityModule("FPS/TPS/Ping HUD", "Shows FPS locally; TPS/ping need server telemetry and remain partial.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.FPS_HUD));
        register(new ModernUtilityModule("Armor HUD", "Armor/durability HUD configuration; render hook pending.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Durability HUD", "Durability HUD configuration; True Durability tooltips are fully working.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Potion HUD", "Potion HUD customization; render hook pending.", Category.RENDER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.VISUAL_SETTINGS));

        register(new ModernUtilityModule("Sprint", "Holds vanilla sprint while moving forward.", Category.MOVEMENT, ModuleStatus.WORKING, ModernUtilityModule.Behavior.SPRINT));
        register(new ModernUtilityModule("Sneak", "Holds the vanilla sneak key while enabled.", Category.MOVEMENT, ModuleStatus.WORKING, ModernUtilityModule.Behavior.SNEAK));
        register(new ModernUtilityModule("Auto Walk", "Holds the vanilla forward key while enabled.", Category.MOVEMENT, ModuleStatus.WORKING, ModernUtilityModule.Behavior.AUTO_WALK));
        register(new ModernUtilityModule("Step", "Disabled: changing step height safely requires compatibility-tested movement hooks.", Category.MOVEMENT, ModuleStatus.UNSAFE_DISABLED, ModernUtilityModule.Behavior.UNSAFE_STUB));
        register(new ModernUtilityModule("No Fall", "Disabled: modern servers validate fall damage; packet spoofing is unsafe/not portable.", Category.MOVEMENT, ModuleStatus.UNSAFE_DISABLED, ModernUtilityModule.Behavior.UNSAFE_STUB));
        register(new ModernUtilityModule("Elytra Helper", "Logger-only elytra assistance is provided by Elytra Flight Logger.", Category.MOVEMENT, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.PASSIVE));
        register(new ModernUtilityModule("Boat Fly", "Disabled: boat/fly packet movement is unsafe and server-specific on 1.21.x.", Category.MOVEMENT, ModuleStatus.UNSAFE_DISABLED, ModernUtilityModule.Behavior.UNSAFE_STUB));

        register(new ModernUtilityModule("Auto Tool", "Safe configuration shell; inventory slot switching requires input hooks before working.", Category.PLAYER, ModuleStatus.PLACEHOLDER, ModernUtilityModule.Behavior.PASSIVE));
        register(new ModernUtilityModule("Auto Eat", "Safe configuration shell; automatic food use requires guarded item-use hooks.", Category.PLAYER, ModuleStatus.PLACEHOLDER, ModernUtilityModule.Behavior.PASSIVE));
        register(new ModernUtilityModule("Auto Totem", "Disabled until modern inventory/offhand swaps are compatibility-tested.", Category.PLAYER, ModuleStatus.UNSAFE_DISABLED, ModernUtilityModule.Behavior.UNSAFE_STUB));
        register(new ModernUtilityModule("Inventory Move", "Disabled: moving in screens needs mixins and can conflict with server interactions.", Category.PLAYER, ModuleStatus.UNSAFE_DISABLED, ModernUtilityModule.Behavior.UNSAFE_STUB));
        register(new ModernUtilityModule("Fast Place", "Disabled: use-rate changes are server-validated and not portable.", Category.PLAYER, ModuleStatus.UNSAFE_DISABLED, ModernUtilityModule.Behavior.UNSAFE_STUB));
        register(new ModernUtilityModule("Middle Click Friend", "Friend management entry point; use .friend once command storage is configured.", Category.PLAYER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.FRIENDS));
        register(new ModernUtilityModule("Friend System", "Persistent friend-list module shell for commands/config.", Category.PLAYER, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.FRIENDS));

        register(new ModernUtilityModule("Auto Reconnect", "Configuration shell; reconnect screen hooks are pending.", Category.WORLD, ModuleStatus.PLACEHOLDER, ModernUtilityModule.Behavior.PASSIVE));
        register(new ModernUtilityModule("Auto Respawn", "Requests vanilla respawn when the death screen is active.", Category.WORLD, ModuleStatus.WORKING, ModernUtilityModule.Behavior.AUTO_RESPAWN));
        register(new ModernUtilityModule("Chat Logger", "Persistent chat logging command/config entry point.", Category.WORLD, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.CHAT_LOGGER));
        register(new ModernUtilityModule("Death Coordinates", "Prints your coordinates once when the client observes death.", Category.WORLD, ModuleStatus.WORKING, ModernUtilityModule.Behavior.DEATH_COORDINATES));
        register(new ModernUtilityModule("Waypoints", "Waypoint storage entry point for .waypoint/config JSON.", Category.WORLD, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.WAYPOINTS));
        register(new ModernUtilityModule("Base/POI Notes", "Local base/POI note storage entry point.", Category.WORLD, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.NOTES));
        register(new ModernUtilityModule("Screenshot/Location Logger", "Location logging shell; screenshot capture hook pending.", Category.WORLD, ModuleStatus.PLACEHOLDER, ModernUtilityModule.Behavior.NOTES));

        register(new ModernUtilityModule("Notifications", "Local notification preferences for module messages.", Category.MISC, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.PASSIVE));
        register(new ModernUtilityModule("Module List HUD", "Shows active-module status reminders and saves HUD settings.", Category.MISC, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.MODULE_LIST_HUD));
        register(new ModernUtilityModule("ClickGUI Settings", "Exposes per-GUI and per-category color/layout settings.", Category.MISC, ModuleStatus.WORKING, ModernUtilityModule.Behavior.VISUAL_SETTINGS));
        register(new ModernUtilityModule("Config Profiles", "Multiple config profile metadata is saved; profile switching command is partial.", Category.MISC, ModuleStatus.PARTIAL, ModernUtilityModule.Behavior.PASSIVE));
        register(new ModernUtilityModule("Panic/Disable All", "Use .panic to disable all non-command modules.", Category.MISC, ModuleStatus.WORKING, ModernUtilityModule.Behavior.PANIC));
        register(new ModernUtilityModule("Keybind Manager", "Keybinds save per module and can be changed with .bind.", Category.MISC, ModuleStatus.WORKING, ModernUtilityModule.Behavior.PASSIVE));

        register(new ModernUtilityModule("Criticals", "Client-side combat helper placeholder only; no packets or aura behavior.", Category.COMBAT, ModuleStatus.PLACEHOLDER, ModernUtilityModule.Behavior.PASSIVE));
        register(new ModernUtilityModule("Totem Pop Counter", "Safe combat information module shell; event display pending.", Category.COMBAT, ModuleStatus.PLACEHOLDER, ModernUtilityModule.Behavior.PASSIVE));
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
