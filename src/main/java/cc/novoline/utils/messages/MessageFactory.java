package cc.novoline.utils.messages;

import cc.novoline.utils.messages.HelpMessage.UsageMessage;
import cc.novoline.utils.messages.IRCMessage.ChatMessage;
import cc.novoline.utils.messages.IRCMessage.ConfigInfoMessage;
import cc.novoline.utils.messages.IRCMessage.ConfigListMessage;
import cc.novoline.utils.messages.IRCMessage.DirectMessage;
import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.skidunion.irc.entities.ChatEntity;
import net.skidunion.irc.entities.ConfigEntity;
import net.skidunion.irc.entities.UserEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author xDelsy
 */
public final class MessageFactory {

	public static @NotNull TextMessage text(@Nullable String text) {
		return TextMessage.of(text);
	}

	public static @NotNull TextMessage text(@Nullable String text, @Nullable EnumChatFormatting color) {
		return TextMessage.of(text, color);
	}

	public static @NotNull HelpMessage help(@NotNull String name, @NotNull String command, @NotNull UsageMessage... subCommands) {
		return HelpMessage.of(name, command, subCommands);
	}

	public static @NotNull UsageMessage usage(@NotNull String command, @NotNull String description) {
		return UsageMessage.of(command, description);
	}

	public static @NotNull IRCMessage broadcast(@NotNull String message) {
		return IRCMessage.of(message);
	}

	public static @NotNull ChatMessage chat(@NotNull ChatEntity chatEntity) {
		return ChatMessage.of(chatEntity);
	}

	public static @NotNull ConfigInfoMessage configInfo(@NotNull ConfigEntity configEntity) {
		return ConfigInfoMessage.of(configEntity);
	}

	public static @NotNull ConfigListMessage configList(@NotNull List<ConfigEntity> configs, boolean self) {
		return ConfigListMessage.of(configs, self);
	}

	public static @NotNull DirectMessage reply(@NotNull UserEntity user, @NotNull String message) {
		return DirectMessage.reply(user, message);
	}

	public static @NotNull DirectMessage receive(@NotNull UserEntity user, @NotNull String message) {
		return DirectMessage.receive(user, message);
	}

	public static @NotNull Message empty() {
		return EmptyMessage.get();
	}

	@Contract(value = "-> fail", pure = true)
	private MessageFactory() {
		throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
