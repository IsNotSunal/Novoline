package cc.novoline.commands;

import cc.novoline.Novoline;
import cc.novoline.commands.impl.*;
import cc.novoline.commands.impl.DirectMessageCommand.ReplyCommand;
import net.minecraft.command.CommandHandler;
import net.skidunion.security.annotations.Protect;
import org.jetbrains.annotations.NotNull;

/**
 * @author xDelsy
 */

@Protect
public class NovoCommandHandler extends CommandHandler {

	public NovoCommandHandler(@NotNull Novoline novoline) {
		super(".");
		registerCommands(novoline);
	}

	public void registerCommands(Novoline novoline) {
		registerCommand(new TargetCommand(novoline));
		registerCommand(new BindCommand(novoline));
		registerCommand(new ConfigCommand(novoline));
		registerCommand(new FriendCommand(novoline));
		registerCommand(new NameCommand(novoline));
		registerCommand(new ToggleCommand(novoline));
		registerCommand(new VClipCommand(novoline));
		registerCommand(new WaypointCommand(novoline));
		registerCommand(new PanicCommand(novoline));
		registerCommand(new HideCommand(novoline));
		registerCommand(new StatusCommand(novoline));
		registerCommand(new ChatCommand(novoline));
		registerCommand(new IRCCommand(novoline));
		registerCommand(new DirectMessageCommand(novoline));
		registerCommand(new ReplyCommand(novoline));
		registerCommand(new KillsultsCommand(novoline));
		registerCommand(new TeleportCommand(novoline));
		registerCommand(new RenameCommand(novoline));

		registerCommand(new TestCommand(novoline));
	}
}
