package com.jesus_crie.iut2_bot.command;

import com.jesus_crie.iut2_bot.Icons;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.annotation.Nonnull;

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
        super(AccessLevel.CREATOR);
    }

    @RegisterPattern
    public void onStop(@Nonnull final CommandEvent event) {
        event.getChannel().sendMessage(MESSAGE).complete();
        event.getModule().getBot().shutdown();
    }
}
