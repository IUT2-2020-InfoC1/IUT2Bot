package com.jesus_crie.iut2_bot.command;

import com.jesus_crie.iut2_bot.Icons;
import com.jesus_crie.iut2_bot.Utils;
import com.jesus_crie.iut2_bot.timetable.IUTGroup;
import com.jesus_crie.iut2_bot.timetable.Lesson;
import com.jesus_crie.iut2_bot.timetable.TimetableModule;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterArgument;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import com.jesus_crie.modularbot_command.processing.Option;
import com.jesus_crie.modularbot_command.processing.Options;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@CommandInfo(
        name = {"edt", "timetable"},
        description = "Affiche l'emploi du temps de la journée.",
        options = {"DETAILS"}
)
public class TimetableCommand extends Command {

    // Options
    @RegisterArgument
    public static final Option<Void> ALL = new Option<>("all", 'a');
    @RegisterArgument
    public static final Option<Void> DETAILS = new Option<>("details", 'd');

    static {
        Option.registerOptions(ALL, DETAILS);
    }

    private static final MessageEmbed MESSAGE_ERROR_NO_GROUP = Utils.getErrorMessage("Vous n'êtes dans aucun groupe !", null);
    private static final String MESSAGE_HEADER_WARNING = "```diff\n- Attention, les changements d'emploi du temps risquent de ne pas apparaitre !```";
    private static final String MESSAGE_HEADER_DAY_FORMAT = "%1$s %3$td %2$s %3$tY - Groupe %4$s";
    private static final String MESSAGE_FOOTER_TIME_FORMAT = "Plus que %1$tHh%1$tM avant la fin de la journée !";
    private static final String MESSAGE_LESSON_DETAIL_HEADER_FORMAT = Icons.EMOTE_DIAMOND_ORANGE + " [%s > %s]: %s    [%s]";
    private static final String MESSAGE_LESSON_DETAIL_CONTENT_FORMAT = "**Durée**: `%s`\n**Module**: `%s`\n**Prof**: %s\n**Groupes**: `%s`";
    private static final String MESSAGE_LESSON_SIMPLE_FORMAT = "`%s > %s` (`%s`): %s    [`%s`]";
    private static final String MESSAGE_LESSON_SIMPLE_CURRENT_FORMAT = "**`%s > %s` (`%s`): %s    [`%s`]**";
    private static final String MESSAGE_LESSON_SIMPLE_DONE_FORMAT = "~~`%s > %s` (`%s`): %s    [`%s`]~~";

    private final TimetableModule module;

    public TimetableCommand(@Nonnull final TimetableModule module) {
        super(AccessLevel.GUILD_ONLY);
        this.module = module;
    }

    @RegisterPattern
    public void onDefault(@Nonnull final CommandEvent event, @Nonnull final Options options) {
        final IUTGroup group = getGroup(event);
        if (group == null) {
            event.getChannel().sendMessage(MESSAGE_ERROR_NO_GROUP).queue();
            return;
        }

        // Get today date
        final Calendar date = Calendar.getInstance(Locale.FRANCE);
        boolean isToday = true;

        // If it's the weekend, take the next monday at midnight
        if (isWeekEnd(date)) {
            isToday = false;
            date.add(Calendar.WEEK_OF_YEAR, 1);
            date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }

        final List<Lesson> lessons = module.queryDayForGroup(group, date);
        lessons.sort(null); // Sort by date

        final EmbedBuilder builder = createEmbedForDay(lessons, isToday ? date : null, options.has(DETAILS))
                .setColor(group.getColor())
                .setAuthor(
                        String.format(MESSAGE_HEADER_DAY_FORMAT,
                                Utils.translateCalendarDay(date.get(Calendar.DAY_OF_WEEK)),
                                Utils.translateCalendarMonth(date.get(Calendar.MONTH)),
                                date.getTimeInMillis(),
                                group.getShortName()),
                        null, Icons.ICON_INFORMATION);

        if (isToday) {
            builder.setFooter(String.format(MESSAGE_FOOTER_TIME_FORMAT,
                    lessons.get(lessons.size() - 1).getTime().getEnd() - date.getTimeInMillis()), null);
        }

        event.getChannel().sendMessage(new MessageBuilder(MESSAGE_HEADER_WARNING).setEmbed(builder.build()).build()).queue();
    }

    @Nonnull
    private EmbedBuilder createEmbedForDay(@Nonnull final List<Lesson> lessons, @Nullable final Calendar date, final boolean details) {
        final EmbedBuilder builder = new EmbedBuilder();

        if (details) {
            // Print with details
            for (Lesson lesson : lessons) {
                builder.addField(
                        String.format(MESSAGE_LESSON_DETAIL_HEADER_FORMAT,
                                lesson.getTime().getHourFormat(), lesson.getTime().getEndFormat(),
                                lesson.getInfo().getName(), lesson.getInfo().getRoom()),
                        String.format(MESSAGE_LESSON_DETAIL_CONTENT_FORMAT,
                                lesson.getTime().getDurationFormat(), lesson.getInfo().getCode(),
                                lesson.getInfo().getTeacher(), lesson.getGroupsFormat()),
                        false);
            }

        } else {
            // Print short version
            for (Lesson lesson : lessons) {
                String format = MESSAGE_LESSON_SIMPLE_FORMAT;

                // If the lesson
                if (date != null && lesson.getTime().getStart() < date.getTimeInMillis()) {
                    if (lesson.getTime().getEnd() < date.getTimeInMillis())
                        format = MESSAGE_LESSON_SIMPLE_DONE_FORMAT;
                    else format = MESSAGE_LESSON_SIMPLE_CURRENT_FORMAT;
                }

                builder.appendDescription(
                        String.format(format,
                                lesson.getTime().getHourFormat(), lesson.getTime().getEndFormat(), lesson.getTime().getDurationFormat(),
                                lesson.getInfo().getName(), lesson.getInfo().getRoom())
                ).appendDescription("\n");
            }
        }

        return builder;
    }

    @Nullable
    private IUTGroup getGroup(@Nonnull final CommandEvent event) {
        for (Role role : event.getMember().getRoles()) {
            if (GroupCommand.GROUP_PATTERN.matcher(role.getName()).matches()) {
                return IUTGroup.fromShortName(role.getName());
            }
        }

        return null;
    }

    private boolean isWeekEnd(@Nonnull final Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }
}
