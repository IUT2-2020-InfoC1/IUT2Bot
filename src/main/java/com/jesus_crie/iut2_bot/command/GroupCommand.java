package com.jesus_crie.iut2_bot.command;

import com.jesus_crie.iut2_bot.Icons;
import com.jesus_crie.iut2_bot.Utils;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;

@CommandInfo(
        name = {"group", "g"},
        description = "Used to join a group."
)
public class GroupCommand extends Command {

    public GroupCommand() {
        super(AccessLevel.GUILD_ONLY);
    }

    private static final MessageEmbed MESSAGE_ERROR_INVALID_GROUP = Utils.getErrorMessage("Nom de groupe invalide !",
            "Le nom du group doit être une lettre entre A et D suivi d'un chiffre entre 1 et 2.");

    private static final MessageEmbed MESSAGE_ERROR_GROUP_NOT_FOUND = Utils.getErrorMessage("Aucun role ne correspond à ce groupe !", null);

    public void onGroup(@Nonnull final CommandEvent event, @Nonnull final String group) {
        if (group.length() == 2 && group.matches("[a-dA-D][12]")) {
            try {
                final Role role = event.getGuild().getRolesByName(group, true).get(0);
                role.getGuild().getController().addSingleRoleToMember(event.getMember(), role).complete();

                final EmbedBuilder builder = new EmbedBuilder()
                        .setAuthor("Vous avez été ajouter au groupe: " + role.getName() + " !", null, Icons.ICON_ACCCEPT)
                        .setColor(0x00ff00);

                event.getChannel().sendMessage(builder.build()).queue();

            } catch (IndexOutOfBoundsException e) {
                // Should not happen but meh.
                event.getChannel().sendMessage(MESSAGE_ERROR_GROUP_NOT_FOUND).queue();
            }
        } else event.getChannel().sendMessage(MESSAGE_ERROR_INVALID_GROUP).queue();
    }
}
