package com.jesus_crie.iut2_bot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;

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

    public static MessageEmbed getErrorMessagePermission(@Nonnull final Permission permission) {
        return new EmbedBuilder()
                .setAuthor("Je n'ai pas la permission de faire ça !", null, Icons.ICON_DENY)
                .setColor(0xff0000)
                .appendDescription("Merci d'aller taper un admin et pas le créateur du bot !\n")
                .appendDescription("Permission manquante: **" + permission.getName() + "**")
                .build();
    }

    public static String translateCalendarDay(final int day) {
        switch (day) {
            case Calendar.MONDAY:
                return "Lundi";
            case Calendar.TUESDAY:
                return "Mardi";
            case Calendar.WEDNESDAY:
                return "Mercredi";
            case Calendar.THURSDAY:
                return "Jeudi";
            case Calendar.FRIDAY:
                return "Vendredi";
            case Calendar.SATURDAY:
                return "Samedi";
            case Calendar.SUNDAY:
                return "Dimanche";
            default:
                return "Unknown";
        }
    }

    public static String translateCalendarMonth(final int month) {
        switch (month) {
            case Calendar.JANUARY:
                return "Janvier";
            case Calendar.FEBRUARY:
                return "Février";
            case Calendar.MARCH:
                return "Mars";
            case Calendar.APRIL:
                return "Avril";
            case Calendar.MAY:
                return "Mai";
            case Calendar.JUNE:
                return "Juin";
            case Calendar.JULY:
                return "Juillet";
            case Calendar.AUGUST:
                return "Août";
            case Calendar.SEPTEMBER:
                return "Septembre";
            case Calendar.OCTOBER:
                return "Octobre";
            case Calendar.NOVEMBER:
                return "Novembre";
            case Calendar.DECEMBER:
                return "Décembre";
            default:
                return "Unknown";
        }
    }
}
