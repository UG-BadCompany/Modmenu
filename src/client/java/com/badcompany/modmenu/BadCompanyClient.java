package com.badcompany.modmenu;

import com.badcompany.modmenu.commands.CommandManager;
import com.badcompany.modmenu.config.ConfigManager;
import com.badcompany.modmenu.gui.ClickGuiScreen;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public final class BadCompanyClient implements ClientModInitializer {
    public static final String MOD_ID = "badcompany";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String KEY_CATEGORY = "key.category.badcompany";
    public static KeyBinding OPEN_GUI_KEY;

    private static final ModuleManager MODULES = new ModuleManager();
    private static ConfigManager configManager;
    private static CommandManager commandManager;
    private final Set<InputUtil.Key> pressedModuleKeys = new HashSet<>();

    public static ModuleManager modules() { return MODULES; }
    public static ConfigManager config() { return configManager; }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing BadCompany Fabric client");
        OPEN_GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.badcompany.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                KEY_CATEGORY
        ));

        MODULES.registerDefaults();
        configManager = new ConfigManager(MODULES);
        MODULES.attachConfig(configManager);
        configManager.loadSafely(OPEN_GUI_KEY);
        commandManager = new CommandManager(MODULES);

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configManager.saveSafely());
        ClientSendMessageEvents.ALLOW_CHAT.register(commandManager::handleChat);
        LOGGER.info("BadCompany initialized with {} modules", MODULES.modules().size());
    }

    private void onClientTick(MinecraftClient client) {
        while (OPEN_GUI_KEY.wasPressed()) {
            client.setScreen(new ClickGuiScreen(MODULES, configManager));
        }
        handleModuleKeybinds(client);
        MODULES.tick();
    }

    private void handleModuleKeybinds(MinecraftClient client) {
        if (client.getWindow() == null) return;
        for (Module module : MODULES.modules()) {
            InputUtil.Key key = module.keybind();
            if (key == null || key == InputUtil.UNKNOWN_KEY || key.getCategory() != InputUtil.Type.KEYSYM) continue;
            boolean down = InputUtil.isKeyPressed(client.getWindow().getHandle(), key.getCode());
            if (down && pressedModuleKeys.add(key)) {
                MODULES.handleKey(key);
            } else if (!down) {
                pressedModuleKeys.remove(key);
            }
        }
    }
}
