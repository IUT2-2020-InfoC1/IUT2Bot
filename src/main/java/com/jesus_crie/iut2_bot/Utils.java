package com.jesus_crie.iut2_bot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Utils {

    @Nonnull
    public static MessageEmbed getErrorMessage(@Nonnull final String title, @Nullable final String content) {
        final EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(title, null, Icons.ICON_DENY)
                .setColor(0xff0000);
        if (content != null && content.length() > 0)
            builder.setDescription(content);

        return builder.build();
    }
}
