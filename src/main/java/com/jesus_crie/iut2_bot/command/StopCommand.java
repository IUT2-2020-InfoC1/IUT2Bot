package com.jesus_crie.iut2_bot.command;

import com.jesus_crie.iut2_bot.Icons;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.EnumSet;

@CommandInfo(
        name = {"stop", "shutdown"},
        description = "Stops the bot."
)
public class StopCommand extends Command {

    private static final MessageEmbed MESSAGE = new EmbedBuilder()
            .setAuthor("Shutting down...", null, Icons.ICON_BED)
            .setColor(0x8823eb)
            .build();

    public StopCommand() {
        super(new AccessLevel(EnumSet.noneOf(Permission.class), false, false) {
            @Override
            public boolean check(@Nonnull final CommandEvent event) {
                return event.getAuthor().getIdLong() == AccessLevel.CREATOR_ID
                        || event.getAuthor().getIdLong() == 164423900208300032L;
            }
        });
    }

    @RegisterPattern
    public void onStop(@Nonnull final CommandEvent event) {
        event.getChannel().sendMessage(MESSAGE).complete();
        event.getModule().getBot().shutdown();
    }
}
