package cc.novoline.commands.impl;

import cc.novoline.Novoline;
import cc.novoline.commands.NovoCommand;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.skidunion.irc.entities.MinecraftServerEntity;
import net.skidunion.irc.requests.RequestError;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;

import static cc.novoline.utils.messages.MessageFactory.usage;
import static cc.novoline.utils.notifications.NotificationType.INFO;
import static net.minecraft.util.EnumChatFormatting.RED;

public class IRCCommand extends NovoCommand {

    public IRCCommand(@NonNull Novoline novoline) {
        super(novoline, "irc", "Manage IRC", "i");
    }

    private boolean hideToggled = false;

    @Override
    public void process(String[] args) {
        if (args.length < 1) {
            sendHelp("IRC help:", ".irc",
                    usage("kick (username)", "kicks a user out of the client"),
                    usage("broadcast (message)", "broadcast a message to all users"),
                    usage("mute (username) (duration in minutes)", "mutes a user in the public chat"),
                    usage("unmute (username)", "unmutes a user"),
                    usage("hide", "Hide yourself from the tab menu")
            );

            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "hide":
                if(Novoline.getInstance().getIRC().isAuthenticated()) {
                    if(hideToggled = !hideToggled) {
                        Novoline.getInstance().getIRC().update(new MinecraftServerEntity(null, null)).queue();

                        Novoline.getInstance().getNotificationManager()
                                .pop("You've hidden yourself",   INFO);
                    } else {
                        Novoline.getInstance().getIRC().update(new MinecraftServerEntity(
                                Minecraft.getInstance().session.getUsername(),
                                Minecraft.getInstance().getCurrentServerData() == null ?
                                                                        "Singleplayer" :
                                                                        Minecraft.getInstance().getCurrentServerData().serverIP
                                )).queue();

                        Novoline.getInstance().getNotificationManager()
                                .pop("You've unhidden yourself", INFO);
                    }
                }

                break;

            case "kick":
            case "unmute":
                if (args.length < 2) {
                    send("Usage: .irc " + command + " (username)", RED);
                    return;
                }

            case "broadcast":
                if (args.length < 2) {
                    send("Usage: .irc " + command + " (message)", RED);
                    return;
                }

            case "mute":
                if (args.length < 3 && command.equals("mute") /* гей костыль */) {
                    send("Usage: .irc " + command + " (username) (duration in minutes)", RED);
                    return;
                }

                if (novoline.getIRC().isAuthenticated()) {
                    try {
                        switch (command) {
                            case "kick": {
                                novoline.getIRC().kickUser(args[1]).complete();
                                send("Kicked user " + args[1] + "!");

                                return;
                            }

                            case "unmute": {
                                novoline.getIRC().unmute(args[1]).complete();
                                send("Unmuted user " + args[1]);
                                return;
                            }

                            case "broadcast": {
                                String message = Strings.join(Arrays.copyOfRange(args, 1, args.length), " ");
                                novoline.getIRC().broadcastMessage(message).complete();

                                return;
                            }

                            case "mute": {
                                novoline.getIRC().mute(args[1], Integer.parseInt(args[2])).complete();
                                send("Muted user " + args[1]);

                                return;
                            }

                            default:
                                send("Unknown subcommand: " + command);
                        }
                    } catch (RequestError ex) {
                        send(ex.getMessage(), RED);
                    } catch (NumberFormatException ex) {
                        send("An argument is expected to be a number", RED);
                    }
                }
        }
    }
}
