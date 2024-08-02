package cc.novoline.commands.impl;

import cc.novoline.Novoline;
import cc.novoline.commands.NovoCommand;
import cc.novoline.modules.configurations.ConfigManager;
import cc.novoline.modules.exceptions.OutdatedConfigException;
import cc.novoline.modules.exceptions.ReadConfigException;
import cc.novoline.utils.messages.MessageFactory;
import cc.novoline.utils.messages.TextMessage;
import net.skidunion.irc.requests.RequestError;
import net.skidunion.security.annotations.Protect;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static cc.novoline.utils.messages.MessageFactory.text;
import static cc.novoline.utils.messages.MessageFactory.usage;
import static cc.novoline.utils.notifications.NotificationType.ERROR;
import static cc.novoline.utils.notifications.NotificationType.SUCCESS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.minecraft.util.EnumChatFormatting.*;

@Protect
public final class ConfigCommand extends NovoCommand {

    /* constructors */
    public ConfigCommand(@NonNull Novoline novoline) {
        super(novoline, "config", "Manages configs stuff", Arrays.asList("cfg", "conf", "configs", "configure", "configuration"));
    }

    /* methods */
    @SuppressWarnings("unchecked")
    @Override
    public void process(String[] args) {
        if (args.length < 1) {
            sendHelp( // @off
                    "Configs help:", ".configs",
                    usage("list", "shows all configs"),
                    usage("load (name)", "loads a config"),
                    usage("save (name)", "saves a config"),
                    usage("delete (name)", "removes a config"),
                    usage("onlinelist [self]", "lists online configs"),
                    usage("download (name)", "downloads an online config"),
                    usage("upload (name)", "uploads or updates a config that you own (max 5)"),
                    usage("info (name)", "shows online config info"),
                    usage("remove (name)", "removes an online config")
            ); //@on
            return;
        }

        final ConfigManager configManager = this.novoline.getModuleManager().getConfigManager();
        final String command = args[0].toLowerCase();

        switch (command) {
            case "download":
            case "upload":
            case "remove":
            case "info":

            case "load":
            case "save":
            case "delete": {
                if (args.length < 2) {
                    send("Usage: .config " + command + " <name>", RED);
                    return;
                }

                final String configName = args[1];

                switch (command) {
                    case "load": {
                        loadConfig(configManager, configName);
                        return;
                    }
                    case "save": {
                        saveConfig(configManager, configName);
                        return;
                    }
                    case "delete": {
                        deleteConfig(configManager, configName);
                        return;
                    }

                    case "upload": {
                        if (novoline.getIRC().isAuthenticated()) {
                            novoline.getIRC().uploadConfig(configName, saveToString(configManager, configName)).queue(success ->
                                            novoline.getNotificationManager().pop("Config uploaded", 2_000, SUCCESS),
                                    error -> novoline.getNotificationManager().pop(((RequestError) error).getMessage(), 2_000, ERROR));

                            return;
                        }
                    }

                    case "info": {
                        if (novoline.getIRC().isAuthenticated()) {
                            novoline.getIRC().getConfigInfo(configName).queue(config -> send(MessageFactory.configInfo(config)),
                                    error -> novoline.getNotificationManager().pop(error.getMessage(), 2_000, ERROR));
                        }

                        return;
                    }

                    case "remove": {
                        if (novoline.getIRC().isAuthenticated()) {
                            novoline.getIRC().deleteConfig(configName).queue(success -> novoline.getNotificationManager().pop("Online config deleted", 2_000, SUCCESS),
                                    error -> novoline.getNotificationManager().pop(((RequestError) error).getMessage(), 2_000, ERROR));

                            return;
                        }
                    }

                    case "download": {
                        if (novoline.getIRC().isAuthenticated()) {
                            novoline.getIRC().downloadConfig(configName).queue(configData -> {
                                try {
                                    Files.write(novoline.getDataFolder().resolve("configs").resolve("[" + configName + "].novo"), configData.getBytes(UTF_8));
                                    novoline.getNotificationManager().pop("Downloaded config!", 2_000, SUCCESS);
                                } catch (IOException ex) {
                                    novoline.getNotificationManager().pop("Failed to save the config!", 2_000, ERROR);
                                    getLogger().error("An I/O error occurred while saving the config!", ex);
                                }
                            }, error -> novoline.getNotificationManager().pop(error.getMessage(), 2_000, ERROR));
                        }
                    }
                }

                return;
            }

            case "list": {
                final List<@NonNull String> configs = configManager.getConfigs();

                final TextMessage text = text("List of configs:");
                if (configs.isEmpty()) text.append(" (empty)", RED);

                send(text, true);
                configs.forEach(name -> send(text("> ", GRAY).append(name, GREEN)));

                return;
            }

            case "onlinelist": {
                if (novoline.getIRC().isAuthenticated()) {
                    boolean self = args.length == 2 && args[1].equalsIgnoreCase("self");

                    novoline.getIRC().getConfigs(self).queue(configs -> send(MessageFactory.configList(configs, self)),
                            error -> novoline.getNotificationManager().pop(error.getMessage(), 2_000, ERROR));

                    return;
                }
            }

            default: {
                notifyError("Unknown command: " + args[0]);
            }
        }
    }

    public static void loadConfig(@NonNull ConfigManager configManager, @NonNull String name) {
        if (name.trim().isEmpty()) {
            Novoline.getInstance().getNotificationManager().pop("Name may not be blank!", 2_000, ERROR);
            return;
        }

        try {
            configManager.load(name, true);
            Novoline.getInstance().getNotificationManager().pop("Loaded config " + name + "!", 2_000, SUCCESS);
        } catch (FileNotFoundException e) {
            Novoline.getInstance().getNotificationManager().pop("Config not found!", 2_000, ERROR);
        } catch (IOException e) {
            Novoline.getLogger().warn("An I/O error occurred while reading config!", e);
            Novoline.getInstance().getNotificationManager().pop("Cannot read config!", 2_000, ERROR);
        } catch (ObjectMappingException e) {
            Novoline.getLogger().warn("An error occurred while deserializing config!", e);
            Novoline.getInstance().getNotificationManager().pop("Cannot load config!", 2_000, ERROR);
        } catch (OutdatedConfigException e) {
            Novoline.getInstance().getNotificationManager().pop("Config is outdated!", 2_000, ERROR);
        } catch (Throwable e) {
            Novoline.getLogger().warn("An unexpected error occurred while loading config!", e);
            Novoline.getInstance().getNotificationManager().pop("Cannot load config!", 2_000, ERROR);
        }
    }

    public static void saveConfig(@NonNull ConfigManager configManager, @NonNull String name) {
        if (name.trim().isEmpty()) {
            Novoline.getInstance().getNotificationManager().pop("Name may not be blank!", 2_000, ERROR);
            return;
        }

        try {
            configManager.save(name);
            Novoline.getInstance().getNotificationManager().pop("Saved config " + name + "!", 2_000, SUCCESS);
        } catch (ReadConfigException e) {
            Novoline.getLogger().warn("An I/O error occurred while reading config!", e);
            Novoline.getInstance().getNotificationManager().pop("Cannot read config!", 2_000, ERROR);
        } catch (IOException e) {
            Novoline.getLogger().warn("An I/O error occurred while saving config!", e);
            Novoline.getInstance().getNotificationManager().pop("Cannot save config!", 2_000, ERROR);
        } catch (ObjectMappingException e) {
            Novoline.getLogger().warn("An I/O error occurred while serializing config!", e);
            Novoline.getInstance().getNotificationManager().pop("Cannot save config!", 2_000, ERROR);
        } catch (Throwable t) {
            Novoline.getLogger().warn("An unexpected error occurred while saving config!", t);
            Novoline.getInstance().getNotificationManager().pop("Cannot save config!", 2_000, ERROR);
        }
    }

    public static String saveToString(@NonNull ConfigManager configManager, @NonNull String name) {
        if (name.trim().isEmpty()) {
            Novoline.getInstance().getNotificationManager().pop("Name may not be blank!", 2_000, ERROR);
            return null;
        }

        try {
            return configManager.saveToString(name);
        } catch (IOException e) {
            Novoline.getLogger().warn("An I/O error occurred while saving config!", e);
            Novoline.getInstance().getNotificationManager().pop("Cannot save config!", 2_000, ERROR);
        } catch (ObjectMappingException e) {
            Novoline.getLogger().warn("An I/O error occurred while serializing config!", e);
            Novoline.getInstance().getNotificationManager().pop("Cannot save config!", 2_000, ERROR);
        }

        return null;
    }

    public static void deleteConfig(@NonNull ConfigManager configManager, @NonNull String name) {
        if (name.trim().isEmpty()) {
            Novoline.getInstance().getNotificationManager().pop("Name may not be blank!", 2_000, ERROR);
            return;
        }

        try {
            configManager.delete(name);
            Novoline.getInstance().getNotificationManager().pop("Deleted config " + name + "!", 2_000, SUCCESS);
        } catch (FileNotFoundException e) {
            Novoline.getInstance().getNotificationManager().pop("Config not found!", 2_000, ERROR);
        } catch (IOException e) {
            Novoline.getLogger().error("An I/O error occurred while deleting config!", e);
            Novoline.getInstance().getNotificationManager().pop("Cannot delete config!", 2_000, ERROR);
        } catch (Throwable t) {
            Novoline.getLogger().warn("An unexpected error occurred while deleting config!", t);
            Novoline.getInstance().getNotificationManager().pop("Cannot delete config!", 2_000, ERROR);
        }
    }

}
