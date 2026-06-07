package com.badcompany.modmenu.config;

import com.badcompany.modmenu.BadCompanyClient;
import com.badcompany.modmenu.gui.CategoryPanel;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleManager;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.ColorSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import com.badcompany.modmenu.settings.Setting;
import com.badcompany.modmenu.settings.StringSetting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final ModuleManager moduleManager;
    private final Path configDirectory;
    private final Path configFile;
    private ClientConfig config = new ClientConfig();

    public ConfigManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        this.configDirectory = FabricLoader.getInstance().getConfigDir().resolve("badcompany");
        this.configFile = configDirectory.resolve("client.json");
    }

    public void loadSafely(KeyBinding openGuiKey) {
        try {
            Files.createDirectories(configDirectory);
            if (Files.exists(configFile)) {
                try (Reader reader = Files.newBufferedReader(configFile)) {
                    ClientConfig loaded = GSON.fromJson(reader, ClientConfig.class);
                    config = sanitize(loaded);
                }
            }
            config = sanitize(config);
            apply(openGuiKey);
        } catch (IOException | RuntimeException ex) {
            BadCompanyClient.LOGGER.warn("Unable to load BadCompany config from {}; defaults will be used", configFile, ex);
            config = new ClientConfig();
        }
    }

    public void saveSafely() {
        try {
            Files.createDirectories(configDirectory);
            capture();
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException | RuntimeException ex) {
            BadCompanyClient.LOGGER.warn("Unable to save BadCompany config to {}", configFile, ex);
        }
    }

    public ClientConfig config() { return config; }

    public void rememberPanel(CategoryPanel panel) {
        PanelConfig panelConfig = config.gui.panels.computeIfAbsent(panel.category().name(), ignored -> new PanelConfig());
        panelConfig.x = panel.x();
        panelConfig.y = panel.y();
        panelConfig.expanded = panel.expanded();
        panelConfig.userPlaced = true;
    }

    public void cycleGuiBackground() {
        config = sanitize(config);
        config.gui.background = GuiBackground.next(config.gui.background).id();
    }

    public void cycleTheme() {
        config = sanitize(config);
        applyTheme(GuiTheme.next(config.gui.theme));
    }

    public void cycleProfile() {
        config = sanitize(config);
        config.gui.activeProfile = switch (config.gui.activeProfile) {
            case "main" -> "pvp";
            case "pvp" -> "hunting";
            case "hunting" -> "testing";
            default -> "main";
        };
    }

    public GuiBackground guiBackground() {
        config = sanitize(config);
        return GuiBackground.from(config.gui.background);
    }

    public double guiScale() {
        config = sanitize(config);
        return config.gui.uiScale;
    }

    public int panelWidth() {
        config = sanitize(config);
        return config.gui.panelWidth;
    }

    public boolean compactMode() {
        config = sanitize(config);
        return config.gui.compactMode;
    }

    public int accentColor() {
        config = sanitize(config);
        return config.accentColor;
    }

    public int rowHeight() { config = sanitize(config); return config.gui.rowHeight; }
    public double fontScale() { config = sanitize(config); return config.gui.fontScale; }
    public int backgroundOpacity() { config = sanitize(config); return config.gui.backgroundOpacity; }
    public int borderColor() { config = sanitize(config); return config.gui.borderColor; }
    public int categoryHeaderColor(String category) {
        config = sanitize(config);
        return config.gui.categoryColors.getOrDefault(category, config.gui.categoryHeaderColor);
    }
    public int enabledModuleColor() { config = sanitize(config); return config.gui.enabledModuleColor; }
    public int disabledModuleColor() { config = sanitize(config); return config.gui.disabledModuleColor; }
    public int backgroundColor() { config = sanitize(config); return config.gui.backgroundColor; }
    public int textColor() { config = sanitize(config); return config.gui.textColor; }
    public int warningColor() { config = sanitize(config); return config.gui.warningColor; }
    public GuiTheme theme() { config = sanitize(config); return GuiTheme.from(config.gui.theme); }
    public String activeProfile() { config = sanitize(config); return config.gui.activeProfile; }
    public int rowHeightScaled() { return Math.max(10, (int) Math.round(rowHeight() * guiScale())); }
    public int scrollStep() { return Math.max(8, rowHeightScaled() * 3); }

    public void cycleGuiScale() {
        config = sanitize(config);
        double current = config.gui.uiScale;
        config.gui.uiScale = current < 0.9D ? 1.0D : current < 1.15D ? 1.25D : 0.75D;
    }

    public void cyclePanelWidth() {
        config = sanitize(config);
        int current = config.gui.panelWidth;
        config.gui.panelWidth = current < 136 ? 150 : current < 170 ? 180 : 120;
    }

    public void cycleAccentColor() {
        int[] colors = { 0xFFBBBBBB, 0xFF7C4DFF, 0xFF55FFFF, 0xFFFF66AA, 0xFFFFCC4D, 0xFF4CD964 };
        int current = accentColor();
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == current) {
                config.accentColor = colors[(i + 1) % colors.length];
                return;
            }
        }
        config.accentColor = colors[0];
    }

    public void toggleCompactMode() {
        config = sanitize(config);
        config.gui.compactMode = !config.gui.compactMode;
        config.gui.rowHeight = config.gui.compactMode ? 11 : 14;
    }

    public void cycleRowHeight() { config = sanitize(config); config.gui.rowHeight = config.gui.rowHeight < 12 ? 14 : config.gui.rowHeight < 14 ? 16 : 11; }
    public void cycleFontScale() { config = sanitize(config); config.gui.fontScale = config.gui.fontScale < 0.9D ? 1.0D : config.gui.fontScale < 1.1D ? 1.2D : 0.8D; }
    public void cycleBackgroundOpacity() { config = sanitize(config); config.gui.backgroundOpacity = config.gui.backgroundOpacity < 120 ? 170 : config.gui.backgroundOpacity < 210 ? 230 : 90; }
    public void cycleBorderColor() { config = sanitize(config); config.gui.borderColor = nextColor(config.gui.borderColor); }
    public void cycleHeaderColor() { config = sanitize(config); config.gui.categoryHeaderColor = nextColor(config.gui.categoryHeaderColor); }
    public void cycleEnabledColor() { config = sanitize(config); config.gui.enabledModuleColor = nextColor(config.gui.enabledModuleColor); }
    public void cycleDisabledColor() { config = sanitize(config); config.gui.disabledModuleColor = nextColor(config.gui.disabledModuleColor); }

    private void applyTheme(GuiTheme theme) {
        config.gui.theme = theme.id();
        config.gui.compactMode = theme.compact;
        config.gui.panelWidth = theme.panelWidth;
        config.gui.rowHeight = theme.rowHeight;
        config.gui.backgroundOpacity = theme.backgroundOpacity;
        config.accentColor = theme.accentColor;
        config.gui.categoryHeaderColor = theme.headerColor;
        config.gui.backgroundColor = theme.backgroundColor;
        config.gui.borderColor = theme.borderColor;
        config.gui.textColor = theme.textColor;
        config.gui.enabledModuleColor = theme.enabledColor;
        config.gui.disabledModuleColor = theme.disabledColor;
        config.gui.warningColor = theme.warningColor;
    }

    private static int nextColor(int current) {
        int[] colors = { 0xFFBBBBBB, 0xFF7C4DFF, 0xFF55FFFF, 0xFFFF66AA, 0xFFFFCC4D, 0xFF4CD964, 0xFFFF453A };
        for (int i = 0; i < colors.length; i++) if (colors[i] == current) return colors[(i + 1) % colors.length];
        return colors[0];
    }

    public void resetGuiLayout() {
        config = sanitize(config);
        config.gui.panels.clear();
    }

    private static double clamp(double value, double min, double max) {
        if (Double.isNaN(value)) return min;
        return Math.max(min, Math.min(max, value));
    }

    private static ClientConfig sanitize(ClientConfig loaded) {
        ClientConfig safe = loaded == null ? new ClientConfig() : loaded;
        if (safe.gui == null) safe.gui = new GuiConfig();
        if (safe.gui.panels == null) safe.gui.panels = new HashMap<>();
        safe.gui.background = GuiBackground.from(safe.gui.background).id();
        safe.gui.theme = GuiTheme.from(safe.gui.theme).id();
        safe.gui.uiScale = clamp(safe.gui.uiScale, 0.65D, 1.25D);
        safe.gui.fontScale = clamp(safe.gui.fontScale, 0.75D, 1.25D);
        safe.gui.panelWidth = (int) clamp(safe.gui.panelWidth, 104, 180);
        safe.gui.rowHeight = (int) clamp(safe.gui.rowHeight, 10, 18);
        safe.gui.backgroundOpacity = (int) clamp(safe.gui.backgroundOpacity, 0, 255);
        safe.gui.backgroundColor |= 0xFF000000;
        safe.gui.borderColor |= 0xFF000000;
        safe.gui.textColor |= 0xFF000000;
        safe.gui.enabledModuleColor |= 0xFF000000;
        safe.gui.disabledModuleColor |= 0xFF000000;
        safe.gui.warningColor |= 0xFF000000;
        if (safe.gui.activeProfile == null || safe.gui.activeProfile.isBlank()) safe.gui.activeProfile = "main";
        if (safe.gui.categoryColors == null) safe.gui.categoryColors = new HashMap<>();
        if (safe.gui.colorPresets == null) safe.gui.colorPresets = new HashMap<>();
        if (safe.gui.hudElements == null) safe.gui.hudElements = new HashMap<>();
        if (safe.profiles == null) safe.profiles = new HashMap<>();
        if (safe.friends == null) safe.friends = new HashMap<>();
        if (safe.waypoints == null) safe.waypoints = new HashMap<>();
        if (safe.searchPresets == null) safe.searchPresets = new HashMap<>();
        if (safe.modules == null) safe.modules = new HashMap<>();
        return safe;
    }

    private void apply(KeyBinding openGuiKey) {
        if (config.openGuiKey != null && !config.openGuiKey.isBlank()) {
            try {
                openGuiKey.setBoundKey(InputUtil.fromTranslationKey(config.openGuiKey));
            } catch (RuntimeException ex) {
                BadCompanyClient.LOGGER.warn("Invalid GUI keybind '{}' in config", config.openGuiKey, ex);
            }
        }
        for (Module module : moduleManager.modules()) {
            ModuleConfig saved = config.modules.get(module.name());
            if (saved == null) {
                module.setEnabled(module.enabledByDefault());
                continue;
            }
            module.setEnabled(saved.enabled);
            if (saved.keybind != null && !saved.keybind.isBlank()) {
                try {
                    module.setKeybind(InputUtil.fromTranslationKey(saved.keybind));
                } catch (RuntimeException ex) {
                    BadCompanyClient.LOGGER.warn("Invalid keybind '{}' for module '{}'", saved.keybind, module.name(), ex);
                }
            }
            applySettings(module, saved);
        }
    }

    private void capture() {
        config = sanitize(config);
        if (BadCompanyClient.OPEN_GUI_KEY != null) {
            config.openGuiKey = BadCompanyClient.OPEN_GUI_KEY.getBoundKeyTranslationKey();
        }
        for (Module module : moduleManager.modules()) {
            ModuleConfig moduleConfig = config.modules.computeIfAbsent(module.name(), ignored -> new ModuleConfig());
            moduleConfig.enabled = module.enabled();
            moduleConfig.keybind = module.keybind().getTranslationKey();
            captureSettings(module, moduleConfig);
        }
    }

    private void applySettings(Module module, ModuleConfig saved) {
        if (saved.settings == null) saved.settings = new HashMap<>();
        for (Setting<?> setting : module.settings()) {
            Object value = saved.settings.get(setting.name());
            if (value == null) continue;
            try {
                if (setting instanceof BooleanSetting booleanSetting && value instanceof Boolean bool) {
                    booleanSetting.set(bool);
                } else if (setting instanceof NumberSetting numberSetting && value instanceof Number number) {
                    numberSetting.set(number.doubleValue());
                } else if (setting instanceof ColorSetting colorSetting && value instanceof Number number) {
                    colorSetting.set(number.intValue());
                } else if (setting instanceof ColorSetting colorSetting && value instanceof String text) {
                    colorSetting.set(parseColor(text, colorSetting.get()));
                } else if (setting instanceof StringSetting stringSetting) {
                    stringSetting.set(String.valueOf(value));
                }
            } catch (RuntimeException ex) {
                BadCompanyClient.LOGGER.warn("Invalid saved value for setting '{}' on module '{}'", setting.name(), module.name(), ex);
            }
        }
    }

    private void captureSettings(Module module, ModuleConfig moduleConfig) {
        if (moduleConfig.settings == null) moduleConfig.settings = new HashMap<>();
        for (Setting<?> setting : module.settings()) {
            Object value = setting.get();
            moduleConfig.settings.put(setting.name(), value);
        }
    }

    private static int parseColor(String text, int fallback) {
        String normalized = text == null ? "" : text.trim();
        if (normalized.startsWith("#")) normalized = normalized.substring(1);
        if (normalized.startsWith("0x") || normalized.startsWith("0X")) normalized = normalized.substring(2);
        if (normalized.isBlank()) return fallback;
        long parsed = Long.parseLong(normalized, 16);
        if (normalized.length() <= 6) parsed |= 0xFF000000L;
        return (int) parsed;
    }

    public static final class ClientConfig {
        public String openGuiKey = "key.keyboard.backslash";
        public GuiConfig gui = new GuiConfig();
        public Map<String, ModuleConfig> modules = new HashMap<>();
        public Map<String, String> profiles = new HashMap<>();
        public Map<String, String> friends = new HashMap<>();
        public Map<String, String> waypoints = new HashMap<>();
        public Map<String, String> searchPresets = new HashMap<>();
        public int accentColor = 0xFF7C4DFF;
    }

    public static final class GuiConfig {
        public String background = GuiBackground.LIGHT_DIM.id();
        public Map<String, PanelConfig> panels = new HashMap<>();
        public double uiScale = 0.85D;
        public double fontScale = 0.9D;
        public int panelWidth = 126;
        public int rowHeight = 11;
        public int backgroundOpacity = 170;
        public int borderColor = 0xFFBBBBBB;
        public int categoryHeaderColor = 0x557C4DFF;
        public int enabledModuleColor = 0xFF7C4DFF;
        public int disabledModuleColor = 0xFFBBBBBB;
        public int backgroundColor = 0xFF080A0F;
        public int textColor = 0xFFECEFF4;
        public int warningColor = 0xFFFF453A;
        public String theme = GuiTheme.CLEAN_MODERN.id();
        public String activeProfile = "main";
        public Map<String, Integer> categoryColors = new HashMap<>();
        public Map<String, Integer> colorPresets = new HashMap<>();
        public Map<String, HudElementConfig> hudElements = new HashMap<>();
        public boolean compactMode = true;
    }

    public enum GuiTheme {
        LEGACY_112("legacy_112", "Legacy", 0xFFBEBEBE, 0x55303030, 0xFF050505, 0xFF9A9A9A, 0xFFE8E8E8, 0xFFBEBEBE, 0xFF8F8F8F, 0xFFFF453A, 150, 126, 11, true),
        FUTURE_DARK("future_dark", "Future", 0xFF9B6DFF, 0x7728193D, 0xFF07080D, 0xFF7E67D8, 0xFFECEBFF, 0xFF9B6DFF, 0xFFA7A1B8, 0xFFFF4D6D, 185, 132, 11, true),
        RUSHER_COMPACT("rusher_compact", "Rusher", 0xFF55D6FF, 0x66214A5A, 0xFF05080A, 0xFF3E6D80, 0xFFEAF8FF, 0xFF55D6FF, 0xFF9EB7C1, 0xFFFFB84D, 175, 120, 10, true),
        CLEAN_MODERN("clean_modern", "Clean", 0xFF4CD964, 0x55305A3A, 0xFF0B0F14, 0xFF304050, 0xFFF2F5F8, 0xFF4CD964, 0xFFAEB7C2, 0xFFFF453A, 180, 136, 12, true),
        HIGH_CONTRAST("high_contrast", "Contrast", 0xFFFFFF00, 0xAA000000, 0xFF000000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF00FF66, 0xFFFFFFFF, 0xFFFF3030, 230, 150, 14, false);

        private final String id;
        private final String label;
        private final int accentColor;
        private final int headerColor;
        private final int backgroundColor;
        private final int borderColor;
        private final int textColor;
        private final int enabledColor;
        private final int disabledColor;
        private final int warningColor;
        private final int backgroundOpacity;
        private final int panelWidth;
        private final int rowHeight;
        private final boolean compact;

        GuiTheme(String id, String label, int accentColor, int headerColor, int backgroundColor, int borderColor, int textColor, int enabledColor, int disabledColor, int warningColor, int backgroundOpacity, int panelWidth, int rowHeight, boolean compact) {
            this.id = id;
            this.label = label;
            this.accentColor = accentColor;
            this.headerColor = headerColor;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.textColor = textColor;
            this.enabledColor = enabledColor;
            this.disabledColor = disabledColor;
            this.warningColor = warningColor;
            this.backgroundOpacity = backgroundOpacity;
            this.panelWidth = panelWidth;
            this.rowHeight = rowHeight;
            this.compact = compact;
        }

        public String id() { return id; }
        public String label() { return label; }

        public static GuiTheme from(String id) {
            for (GuiTheme theme : values()) if (theme.id.equalsIgnoreCase(String.valueOf(id))) return theme;
            return CLEAN_MODERN;
        }

        public static GuiTheme next(String id) {
            GuiTheme current = from(id);
            GuiTheme[] values = values();
            return values[(current.ordinal() + 1) % values.length];
        }
    }

    public enum GuiBackground {
        NONE("none", "None"),
        LIGHT_DIM("light_dim", "Light Dim"),
        DARK_DIM("dark_dim", "Dark Dim"),
        BLUR("blur", "Blur");

        private final String id;
        private final String label;

        GuiBackground(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() { return id; }
        public String label() { return label; }

        public static GuiBackground from(String id) {
            for (GuiBackground background : values()) {
                if (background.id.equalsIgnoreCase(String.valueOf(id))) return background;
            }
            return LIGHT_DIM;
        }

        public static GuiBackground next(String id) {
            GuiBackground current = from(id);
            GuiBackground[] values = values();
            return values[(current.ordinal() + 1) % values.length];
        }
    }

    public static final class PanelConfig {
        public int x;
        public int y;
        public boolean expanded = true;
        public boolean userPlaced;
    }

    public static final class HudElementConfig {
        public int x;
        public int y;
        public boolean enabled = true;
        public double scale = 1.0D;
        public int color = 0xFF4CD964;
    }

    public static final class ModuleConfig {
        public boolean enabled;
        public String keybind = InputUtil.UNKNOWN_KEY.getTranslationKey();
        public Map<String, Object> settings = new HashMap<>();
    }
}
