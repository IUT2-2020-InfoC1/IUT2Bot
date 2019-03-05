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
            .setDescription("Bot par <@182547138729869314> et <@164423900208300032> (C1 ftw)")
            .addField(Icons.EMOTE_DIAMOND_ORANGE + " +help", "Affiche cette aide.", true)
            .addField(Icons.EMOTE_DIAMOND_ORANGE + " +links", "Affiche les liens utiles.", true)
            .addField(Icons.EMOTE_DIAMOND_ORANGE + " +group <group>",
                    "Permet de rejoindre son groupe. Ne peut être fait que si vous n'ête pas déjà dans un groupe !", false)
            .addField(Icons.EMOTE_DIAMOND_ORANGE + " +edt [day|all] [--details]",
                    "Affiche l'emploi du temps d'une journée ou de la semaine **pour son groupe**.\n" +
                            "Usage: ```diff\n" +
                            "+edt\n" +
                            "+edt [lundi|mardi|mercredi|jeudi|vendredi|samedi]\n" +
                            //"+edt all```" +
                            "*Ajouter `--details` à la fin pour avoir plus de details sur les cours.*", false)
            .build();

    @RegisterPattern
    public void onHelp(@Nonnull final CommandEvent event) {
        event.getChannel().sendMessage(MESSAGE).queue();
    }
}
