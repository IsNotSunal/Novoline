package cc.novoline.commands.impl;

import cc.novoline.Novoline;
import cc.novoline.SimpleEventListener;
import cc.novoline.commands.NovoCommand;
import cc.novoline.utils.messages.MessageFactory;
import static cc.novoline.utils.messages.MessageFactory.usage;
import java.util.Arrays;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandException;
import static net.minecraft.util.EnumChatFormatting.RED;
import net.skidunion.irc.requests.RequestError;
import org.jetbrains.annotations.NotNull;

public class DirectMessageCommand extends NovoCommand {

    public DirectMessageCommand(@NotNull Novoline novoline) {
        super(novoline, "message", "Directly message an online user", Arrays.asList("dm", "msg", "m"));
    }

    @Override
    public void process(String[] args) throws CommandException {
        if(args.length < 2) {
            sendHelp( // @off
                    "Message help:", ".message / .dm / .msg / .m",
                    usage("(username) (message)", "directly message a user")
            ); // @on

            return;
        }

        if(!novoline.getIRC().isAuthenticated())
            return;

        String to = args[0];
        String message = Strings.join(Arrays.copyOfRange(args, 1, args.length), " ");

        novoline.getIRC().sendDirectMessage(to, message).queue(success -> {
            send(MessageFactory.reply(novoline.getIRC().getUserManager().getUsers().get(to), message));
            SimpleEventListener.getInstance().setLastMessagedUsername(to);
        }, error -> send(((RequestError)error).getMessage(), RED));
    }

    public static final class ReplyCommand extends NovoCommand {

        public ReplyCommand(@NotNull Novoline novoline) {
            super(novoline, "reply", "Replies to the last messaged user", "r");
        }

        @Override
        public void process(String[] args) throws CommandException {
            if(args.length < 1) {
                sendHelp( // @off
                        "Reply help:", ".reply / .r",
                        usage("(message)", "replies to the last messaged / received user")
                ); // @on

                return;
            }

            if(!novoline.getIRC().isAuthenticated())
                return;

            String message = Strings.join(args, " ");

            novoline.getIRC().sendDirectMessage(SimpleEventListener.getInstance().getLastMessagedUsername(), message).queue(success -> {
                send(MessageFactory.reply(novoline.getIRC().getUserManager().getUsers().get(SimpleEventListener.getInstance().getLastMessagedUsername()), message));
            }, error -> send(((RequestError)error).getMessage(), RED));
        }
    }
}
