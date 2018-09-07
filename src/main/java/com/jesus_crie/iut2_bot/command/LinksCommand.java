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
        name = {"links", "info", "lien"},
        description = "Display some useful links."
)
public class LinksCommand extends Command {

    private static final MessageEmbed MESSAGE = new EmbedBuilder()
            .setAuthor("Liens utiles", null, Icons.ICON_INFORMATION)
            .setColor(0xffffff)
            .appendDescription(Icons.EMOTE_DIAMOND_ORANGE + " **[Chamilo](https://chamilo.iut2.univ-grenoble-alpes.fr)**\n")
            .appendDescription(Icons.EMOTE_DIAMOND_ORANGE + " **[Emploi du temps](http://www-ade.iut2.upmf-grenoble.fr/ade_services/planning_perso/?cas=1)**\n")
            .appendDescription(Icons.EMOTE_DIAMOND_ORANGE + " **[Webmail](https://webmail.etu.univ-grenoble-alpes.fr)**")
            .appendDescription("\n")
            .appendDescription(Icons.EMOTE_DIAMOND_BLUE + " **[Sources du bot](https://github.com/IUT2-2020-InfoC1/IUT2Bot)**")
            .build();

    @RegisterPattern
    public void onInfo(@Nonnull final CommandEvent event) {
        event.getChannel().sendMessage(MESSAGE).queue();
    }
}
