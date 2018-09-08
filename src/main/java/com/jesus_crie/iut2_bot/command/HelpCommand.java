package com.jesus_crie.iut2_bot.command;

import com.jesus_crie.iut2_bot.Icons;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.annotation.Nonnull;

@CommandInfo(
        name = {"help", "h", "?"},
        description = "List available commands."
)
public class HelpCommand extends Command {

    private static final MessageEmbed MESSAGE = new EmbedBuilder()
            .setAuthor("Help", null, Icons.ICON_QUESTION)
            .setColor(0xffffff)
            .addField(Icons.EMOTE_DIAMOND_ORANGE + " +help", "Affiche cette aide.", false)
            .addField(Icons.EMOTE_DIAMOND_ORANGE + " +links", "Affiche les liens utiles.", false)
            .addField(Icons.EMOTE_DIAMOND_ORANGE + " +group <group>", "Permet de rejoindre son groupe.", false)
            .build();

    @RegisterPattern
    public void onHelp(@Nonnull final CommandEvent event) {
        event.getChannel().sendMessage(MESSAGE).queue();
    }
}
