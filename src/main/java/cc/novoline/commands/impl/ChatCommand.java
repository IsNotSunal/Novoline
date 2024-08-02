package cc.novoline.commands.impl;

import cc.novoline.Novoline;
import cc.novoline.commands.NovoCommand;
import net.minecraft.command.CommandException;
import net.minecraft.util.EnumChatFormatting;
import net.skidunion.irc.requests.RequestError;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cc.novoline.utils.messages.MessageFactory.usage;
import static net.minecraft.util.EnumChatFormatting.RED;

public class ChatCommand extends NovoCommand {

    public ChatCommand(@NonNull Novoline novoline) {
        super(novoline, "chat", "Sends a message to the public chat", "c");
    }

    @Override
    public void process(String[] args) throws CommandException {
        if(args.length < 1) {
            sendHelp( // @off
                    "Command help", ".chat (.c)",
                    usage("(message)", description)
            ); // @on

            sendEmpty();
            return;
        }

        String message = String.join(" ", args);

        if(novoline.getIRC().isAuthenticated()) {
            novoline.getIRC().sendChatMessage(message).queue(null, error -> send(((RequestError)error).getMessage(), RED));
        }
    }
}
