package cc.novoline;

import cc.novoline.utils.messages.MessageFactory;
import cc.novoline.utils.notifications.NotificationType;
import net.minecraft.client.Minecraft;
import net.skidunion.irc.entities.MinecraftServerEntity;
import net.skidunion.irc.event.ListenerAdapter;
import net.skidunion.irc.event.impl.ChatMessageReceivedEvent;
import net.skidunion.irc.event.impl.DirectMessageReceivedEvent;
import net.skidunion.irc.event.impl.KillRequestEvent;
import net.skidunion.irc.event.impl.MessageEvent;
import net.skidunion.irc.event.impl.internal.IRCDisconnectedEvent;
import net.skidunion.irc.event.impl.login.SuccessfulLoginEvent;
import org.jetbrains.annotations.NotNull;

public class SimpleEventListener extends ListenerAdapter {

	private enum Holder {
		INSTANCE;

		private final SimpleEventListener value;

		Holder() { this.value = new SimpleEventListener(); }
	}

	public static SimpleEventListener getInstance() {
		return Holder.INSTANCE.value;
	}

	private String lastMessagedUsername;

	@Override
	public void onMessage(@NotNull MessageEvent event) {
		if(Minecraft.getInstance().player != null && !event.getError()) {
			Minecraft.getInstance().player.addChatComponentMessage(MessageFactory.broadcast(event.getMessage()));
		}
	}

	@Override
	public void onChatMessageReceived(@NotNull ChatMessageReceivedEvent event) {
		if(Minecraft.getInstance().player != null) {
			Minecraft.getInstance().player.addChatComponentMessage(MessageFactory.chat(event.getEntity()));
		}
	}

	@Override
	public void onIRCDisconnected(@NotNull IRCDisconnectedEvent event) {
		if(event.getCode() == 4004 && Minecraft.getInstance().player != null) {
			Novoline.getInstance().getNotificationManager().pop(
					"Could not connect to IRC",
					"The authorization token was invalid, please restart the client",
					2_000, NotificationType.ERROR
			);
		}
	}

	@Override
	public void onDirectMessageReceived(@NotNull DirectMessageReceivedEvent event) {
		if(Minecraft.getInstance().player != null) {
			lastMessagedUsername = event.getEntity().getFrom().getUsername();

			Minecraft.getInstance().player.addChatComponentMessage(MessageFactory.receive(event.getEntity().getFrom(), event.getEntity().getMessage()));
		}
	}

	@Override
	public void onKillRequest(@NotNull KillRequestEvent event) {
		System.out.println("Kicked by an administrator.");
		System.exit(0);
	}

	@Override
	public void onSuccessfulLogin(@NotNull SuccessfulLoginEvent event) {
		Novoline.getInstance().getIRC().update(new MinecraftServerEntity(
				Minecraft.getInstance().session.getUsername(),
				Minecraft.getInstance().getCurrentServerData() == null ? "Singleplayer" : Minecraft.getInstance().getCurrentServerData().serverIP
		)).queue();
	}

	//region Lombok
	public String getLastMessagedUsername() {
		return lastMessagedUsername;
	}

	public void setLastMessagedUsername(String lastMessagedUsername) {
		this.lastMessagedUsername = lastMessagedUsername;
	}
	//endregion
}
