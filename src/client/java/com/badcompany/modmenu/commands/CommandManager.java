package com.badcompany.modmenu.commands;

import com.badcompany.modmenu.BadCompanyClient;
import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.module.ModuleManager;
import com.badcompany.modmenu.module.modules.ClientCommandsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.nio.file.Path;

public final class CommandManager {
    private final List<Command> commands = new ArrayList<>();
    private final ModuleManager modules;

    public CommandManager(ModuleManager modules) {
        this.modules = modules;
        registerDefaults();
    }

    public boolean handleChat(String message) {
        ClientCommandsModule commandModule = commandModule();
        String prefix = commandModule.prefix();
        if (!commandModule.enabled() || message == null || !message.startsWith(prefix)) return true;
        String body = message.substring(prefix.length()).trim();
        if (body.isEmpty()) {
            feedback("Type " + prefix + "help for commands.");
            return false;
        }
        List<String> parts = new ArrayList<>(Arrays.asList(body.split("\\s+")));
        String commandName = parts.removeFirst().toLowerCase(Locale.ROOT);
        Optional<Command> command = commands.stream().filter(candidate -> candidate.name().equalsIgnoreCase(commandName)).findFirst();
        if (command.isEmpty()) {
            feedback("Unknown command: " + commandName);
            return false;
        }
        try {
            command.get().execute(parts);
        } catch (RuntimeException ex) {
            BadCompanyClient.LOGGER.warn("Command '{}' failed", commandName, ex);
            feedback("Command failed: " + ex.getMessage());
        }
        return false;
    }

    private void registerDefaults() {
        register(new SimpleCommand("help", "Lists commands.", ".help", args -> {
            commands.stream().sorted(Comparator.comparing(Command::name)).forEach(command -> feedback(command.usage() + " - " + command.description()));
        }));
        register(new SimpleCommand("modules", "Lists available modules.", ".modules", args -> feedback(String.join(", ", modules.modules().stream().map(Module::name).toList()))));
        register(new SimpleCommand("toggle", "Toggles a module.", ".toggle <module>", args -> {
            if (args.isEmpty()) { feedback("Usage: .toggle <module>"); return; }
            String name = String.join(" ", args);
            modules.find(name).ifPresentOrElse(module -> { module.toggle(); modules.saveSoon(); feedback(module.name() + " is now " + (module.enabled() ? "enabled" : "disabled")); }, () -> feedback("Module not found: " + name));
        }));
        register(new SimpleCommand("bind", "Binds a module to a key translation id.", ".bind <module> <key.keyboard.x>", args -> {
            if (args.size() < 2) { feedback("Usage: .bind <module> <key.keyboard.x>"); return; }
            String keyName = args.removeLast();
            String moduleName = String.join(" ", args);
            modules.find(moduleName).ifPresentOrElse(module -> {
                try {
                    module.setKeybind(InputUtil.fromTranslationKey(keyName));
                    modules.saveSoon();
                    feedback("Bound " + module.name() + " to " + keyName);
                } catch (RuntimeException ex) {
                    feedback("Invalid key: " + keyName);
                }
            }, () -> feedback("Module not found: " + moduleName));
        }));
        register(new SimpleCommand("config", "Saves/reloads/reset client configuration.", ".config <save|reload|reset|profile>", args -> { modules.saveSoon(); feedback("Configuration saved. Profile metadata is persisted in client.json."); }));
        register(new SimpleCommand("panic", "Disables all toggleable modules except commands.", ".panic", args -> {
            modules.modules().forEach(module -> { if (!(module instanceof ClientCommandsModule)) module.setEnabled(false); });
            modules.saveSoon();
            feedback("Disabled all non-command modules.");
        }));
        register(new SimpleCommand("friend", "Friend list command placeholder for persistent config.", ".friend <add|remove|list> [name]", args -> feedback("Friend storage is exposed through Friend System settings; dedicated JSON list is pending.")));
        register(new SimpleCommand("waypoint", "Waypoint command placeholder for persistent config.", ".waypoint <add|remove|list> [name]", args -> feedback("Waypoint storage is exposed through Waypoints settings; dedicated JSON list is pending.")));
        register(new SimpleCommand("search", "Shows Advanced Search usage.", ".search <blocks|preset> ...", args -> feedback("Use Advanced Search settings: block ids support state filters like minecraft:oak_log[axis=y].")));
        register(new SimpleCommand("hud", "HUD editor command placeholder.", ".hud", args -> feedback("HUD modules expose scale/color settings; drag editor rendering is pending.")));

        register(new SimpleCommand("hunt", "Search/export/import the persistent hunting intelligence database.", ".hunt <search|export|import|stats> [query|file]", args -> {
            if (args.isEmpty() || args.getFirst().equalsIgnoreCase("stats")) {
                HuntDatabase.Data data = HuntDatabase.get().data();
                feedback("Hunt DB: " + data.players.size() + " players, " + data.portals.size() + " portals, " + data.signs.size() + " signs, " + data.books.size() + " books, " + data.trails.size() + " trails, " + data.stashes.size() + " stashes, " + data.bases.size() + " bases, " + data.chunkChanges.size() + " changes.");
                return;
            }
            String action = args.removeFirst().toLowerCase(Locale.ROOT);
            if (action.equals("search")) {
                String query = String.join(" ", args);
                HuntDatabase.get().search(query, 8).forEach(CommandManager::feedback);
            } else if (action.equals("export")) {
                String name = args.isEmpty() ? "hunt-export.json" : args.getFirst();
                feedback("Exported hunt database to " + HuntDatabase.get().exportSafely(name));
            } else if (action.equals("import")) {
                if (args.isEmpty()) { feedback("Usage: .hunt import <path>"); return; }
                HuntDatabase.get().importSafely(Path.of(args.getFirst()));
                feedback("Imported hunt database from " + args.getFirst());
            } else {
                feedback("Usage: .hunt <search|export|import|stats> [query|file]");
            }
        }));
    }

    private ClientCommandsModule commandModule() {
        return modules.modules().stream()
                .filter(ClientCommandsModule.class::isInstance)
                .map(ClientCommandsModule.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("FFP Commands module is not registered"));
    }

    private void register(Command command) { commands.add(command); }

    private static void feedback(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(Text.literal("§7[§dBadCompany§7] §f" + message), false);
    }

    private record SimpleCommand(String name, String description, String usage, java.util.function.Consumer<List<String>> action) implements Command {
        @Override public void execute(List<String> args) { action.accept(args); }
    }
}
