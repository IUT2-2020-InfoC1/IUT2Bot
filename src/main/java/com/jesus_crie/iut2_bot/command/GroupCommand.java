package com.jesus_crie.iut2_bot.command;

import com.jesus_crie.iut2_bot.Icons;
import com.jesus_crie.iut2_bot.Utils;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.PermissionException;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

@CommandInfo(
        name = {"group", "g"},
        description = "Used to join a group."
)
public class GroupCommand extends Command {

    public static Pattern GROUP_PATTERN = Pattern.compile("[a-dA-D][12]");

    public GroupCommand() {
        super(AccessLevel.GUILD_ONLY);
    }

    private static final MessageEmbed MESSAGE_ERROR_INVALID_GROUP = Utils.getErrorMessage("Nom de groupe invalide !",
            "Le nom du group doit être une lettre entre A et D suivi d'un chiffre entre 1 et 2.");

    private static final MessageEmbed MESSAGE_ERROR_GROUP_NOT_FOUND = Utils.getErrorMessage("Aucun role ne correspond à ce groupe !", null);

    private static final MessageEmbed MESSAGE_ERROR_ALREADY_IN_GROUP = Utils.getErrorMessage("Vous êtes déjà dans un groupe !", null);

    @RegisterPattern(arguments = "STRING")
    public void onGroup(@Nonnull final CommandEvent event, @Nonnull final String group) {
        // Check if given group is a valid one
        if (GROUP_PATTERN.matcher(group).matches()) {
            // Check if the member already have a group role
            if (event.getMember().getRoles().stream().anyMatch(r -> GROUP_PATTERN.matcher(r.getName()).matches())) {
                event.getChannel().sendMessage(MESSAGE_ERROR_ALREADY_IN_GROUP).queue();
                return;
            }

            try {
                // Get the role
                final Role role = event.getGuild().getRolesByName(group, true).get(0);

                // Add the role to the member
                role.getGuild().getController().addSingleRoleToMember(event.getMember(), role).complete();

                final EmbedBuilder builder = new EmbedBuilder()
                        .setAuthor("Vous avez été ajouté au groupe: " + role.getName() + " !", null, Icons.ICON_ACCCEPT)
                        .setColor(0x00ff00);

                event.getChannel().sendMessage(builder.build()).queue();

            } catch (IndexOutOfBoundsException e) {
                // If the corresponding group doesn't have a role
                event.getChannel().sendMessage(MESSAGE_ERROR_GROUP_NOT_FOUND).queue();
            } catch (PermissionException e) {
                // If the bot can't add the role or something else
                event.getChannel().sendMessage(Utils.getErrorMessagePermission(e.getPermission())).queue();
            }
        } else event.getChannel().sendMessage(MESSAGE_ERROR_INVALID_GROUP).queue();
    }
}
