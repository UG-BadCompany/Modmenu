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
    }

    private static ClientConfig sanitize(ClientConfig loaded) {
        ClientConfig safe = loaded == null ? new ClientConfig() : loaded;
        if (safe.gui == null) safe.gui = new GuiConfig();
        if (safe.gui.panels == null) safe.gui.panels = new HashMap<>();
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
        public int accentColor = 0xFF7C4DFF;
    }

    public static final class GuiConfig {
        public Map<String, PanelConfig> panels = new HashMap<>();
    }

    public static final class PanelConfig {
        public int x;
        public int y;
        public boolean expanded = true;
    }

    public static final class ModuleConfig {
        public boolean enabled;
        public String keybind = InputUtil.UNKNOWN_KEY.getTranslationKey();
        public Map<String, Object> settings = new HashMap<>();
    }
}
