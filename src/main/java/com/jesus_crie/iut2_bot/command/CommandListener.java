package com.jesus_crie.iut2_bot.command;

import com.jesus_crie.iut2_bot.Utils;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.exception.CommandProcessingException;
import com.jesus_crie.modularbot_command.exception.UnknownOptionException;
import com.jesus_crie.modularbot_command.listener.NopCommandListener;
import com.jesus_crie.modularbot_command.processing.Options;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.util.List;

public class CommandListener extends NopCommandListener {

    private static final MessageEmbed MESSAGE_ACCESS_LEVEL = Utils.getErrorMessage("Vous n'avez pas le droit d'utiliser cette commande !", null);

    private static final MessageEmbed MESSAGE_FAILED_PROCESS = Utils.getErrorMessage("Failed to process command (syntax error)", null);

    private static final MessageEmbed MESSAGE_NO_PATTERN = Utils.getErrorMessage("Aucun pattern n'a été trouvé pour ces arguments !", null);

    private static final MessageEmbed MESSAGE_UNKNOWN_OPTION = Utils.getErrorMessage("Une option inconnue a été trouvée dans la commande !", null);

    @Override
    public void onTooLowAccessLevel(@Nonnull final CommandEvent event) {
        event.getChannel().sendMessage(MESSAGE_ACCESS_LEVEL).queue();
    }

    @Override
    public void onCommandFailedNoPatternMatch(@Nonnull CommandEvent event, @Nonnull Options options, @Nonnull List<String> arguments) {
        event.getChannel().sendMessage(MESSAGE_NO_PATTERN).queue();
    }

    @Override
    public void onCommandFailedProcessing(@Nonnull CommandEvent event, @Nonnull CommandProcessingException error) {
        final EmbedBuilder builder = new EmbedBuilder(MESSAGE_FAILED_PROCESS);

        final StringBuilder contentBuilder = new StringBuilder(error.getMessage())
                .append("\n")
                .append("```").append(event.getMessage().getContentRaw()).append("\n");

        for (int i = calculateCursorOffset(event.getMessage().getContentRaw()) + error.getCursorPosition(); i > 0; i--)
            contentBuilder.append(" ");

        for (int i = error.getCursorEndPosition() - error.getCursorPosition(); i >= 0; i--)
            contentBuilder.append("^");

        contentBuilder.append("```");

        builder.setDescription(contentBuilder.toString());

        event.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public void onCommandFailedUnknownOption(@Nonnull CommandEvent event, @Nonnull UnknownOptionException error) {
        event.getChannel().sendMessage(new EmbedBuilder(MESSAGE_UNKNOWN_OPTION)
                .setDescription(error.getMessage()).build()).queue();
    }

    private int calculateCursorOffset(@Nonnull final String content) {
        return content.split(" ", 2)[0].length() + 1;
    }
}
