package cc.novoline.utils.messages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import net.minecraft.util.EnumChatFormatting;
import static net.minecraft.util.EnumChatFormatting.*;
import net.skidunion.irc.entities.ChatEntity;
import net.skidunion.irc.entities.ConfigEntity;
import net.skidunion.irc.entities.UserEntity;
import org.jetbrains.annotations.NotNull;

public class IRCMessage extends TextMessage {

    private static final String IRC_PREFIX =
            LIGHT_PURPLE + "IRC" +
                    GRAY + " \u00bb " + RESET;

    protected IRCMessage(@NotNull String message) {
        super(IRC_PREFIX);

        append(" Broadcast: ", RED);
        append(message, WHITE);
    }

    public static @NotNull IRCMessage of(@NotNull String message) {
        return new IRCMessage(message);
    }

    public static final class ChatMessage extends TextMessage {

        private ChatMessage(@NotNull ChatEntity chatEntity) {
            super(IRC_PREFIX);

            append(chatEntity.getFrom().getUsername(), getColor(chatEntity.getFrom().getRankEntity().getName()));
            append(" " + chatEntity.getMessage(), WHITE);
        }

        @NotNull
        public static ChatMessage of(@NotNull ChatEntity chatEntity) {
            return new ChatMessage(chatEntity);
        }
    }

    public static final class ConfigInfoMessage extends TextMessage {

        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        private ConfigInfoMessage(@NotNull ConfigEntity configEntity) {
            super(IRC_PREFIX);

            append("Config info: " + configEntity.getName()).append("\n");
            append(IRC_PREFIX).append("Author: " + configEntity.getOwner().getUsername()).append("\n");
            append(IRC_PREFIX).append("Uploaded: " + DATE_FORMAT.format(new Date(configEntity.getCreationDate().toLong()))).append("\n");
            append(IRC_PREFIX).append("Updated: " + DATE_FORMAT.format(new Date(configEntity.getUpdateDate().toLong())));
        }

        @NotNull
        public static ConfigInfoMessage of(@NotNull ConfigEntity config) {
            return new ConfigInfoMessage(config);
        }
    }

    public static final class ConfigListMessage extends TextMessage {

        private ConfigListMessage(@NotNull List<ConfigEntity> configs, boolean self) {
            super(IRC_PREFIX);

            StringJoiner configsJoiner = new StringJoiner(", ");

            if (self) {
                append("Your configs: \n");
            } else {
                append("Config list: ").append("(", GRAY).append("green", GREEN).append(" - verified)", GRAY).append("\n");
            }

            configs.forEach(config -> {
                if (config.getOwner().getRankEntity().getName().equals("ADMIN")
                        || config.getOwner().getRankEntity().getName().equals("MOD")) {
                    configsJoiner.add(GREEN + config.getName());
                } else {
                    configsJoiner.add(GRAY + config.getName());
                }
            });

            append(configsJoiner.toString());
        }

        @NotNull
        public static ConfigListMessage of(@NotNull List<ConfigEntity> configs, boolean self) {
            return new ConfigListMessage(configs, self);
        }
    }

    public static final class DirectMessage extends TextMessage {

        private DirectMessage(@NotNull UserEntity from, @NotNull String message, boolean reply) {
            super(IRC_PREFIX);

            append("(", GRAY);

            if (reply) {
                append("Me", WHITE);
                append(" -> ", GRAY);
                append(from.getUsername(), getColor(from.getRankEntity().getName()));
            } else {
                append(from.getUsername(), getColor(from.getRankEntity().getName()));
                append(" -> ", GRAY);
                append("Me", WHITE);
            }

            append(") ", GRAY);
            append(message, WHITE);
        }

        public static DirectMessage reply(@NotNull UserEntity from, @NotNull String message) {
            return new DirectMessage(from, message, true);
        }

        public static DirectMessage receive(@NotNull UserEntity from, @NotNull String message) {
            return new DirectMessage(from, message, false);
        }
    }

    private static EnumChatFormatting getColor(String name) {
        switch (name) {
            case "MOD":
                return DARK_PURPLE;
            case "ADMIN":
                return RED;
            default:
                return GRAY;
        }
    }
}
