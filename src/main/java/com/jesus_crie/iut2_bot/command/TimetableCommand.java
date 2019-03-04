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
import com.jesus_crie.modularbot_command.processing.Argument;
import com.jesus_crie.modularbot_command.processing.CommandPattern;
import com.jesus_crie.modularbot_command.processing.Option;
import com.jesus_crie.modularbot_command.processing.Options;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

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
    private static final MessageEmbed MESSAGE_ERROR_INVALID_DAY = Utils.getErrorMessage("Le jour que vous avez donner n'est pas valide !", null);

    private static final String MESSAGE_HEADER_WARNING = "```diff\n- Attention, les changements d'emploi du temps n'apparaissent pas !```";
    private static final String MESSAGE_HEADER_DAY_FORMAT = "%1$s %3$td %2$s %3$tY - Groupe %4$s";
    private static final String MESSAGE_FOOTER_TIME_FORMAT = "Plus que %dh%02d avant la fin de la journée !";
    private static final String MESSAGE_LESSON_DETAIL_HEADER_FUTURE_FORMAT = Icons.EMOTE_HOURGLASS + " [%s > %s]: %s    [%s]";
    private static final String MESSAGE_LESSON_DETAIL_HEADER_CURRENT_FORMAT = Icons.EMOTE_ARROW_RIGHT_BOX + " [%s > %s]: %s    [%s]";
    private static final String MESSAGE_LESSON_DETAIL_HEADER_DONE_FORMAT = Icons.EMOTE_CHECK_MARK_BOX + " [%s > %s]: %s    [%s]";
    private static final String MESSAGE_LESSON_DETAIL_CONTENT_FORMAT = "**Durée**: `%s`\n**Module**: `%s`\n**Prof**: %s\n**Groupes**: `%s`";
    private static final String MESSAGE_LESSON_SIMPLE_FUTURE_FORMAT = Icons.EMOTE_HOURGLASS + " `%s > %s` (`%s`): %s    [`%s`]";
    private static final String MESSAGE_LESSON_SIMPLE_CURRENT_FORMAT = Icons.EMOTE_ARROW_RIGHT_BOX + " **`%s > %s` (`%s`): %s    [`%s`]**";
    private static final String MESSAGE_LESSON_SIMPLE_DONE_FORMAT = Icons.EMOTE_CHECK_MARK_BOX + " ~~`%s > %s` (`%s`): %s    [`%s`]~~";

    private final TimetableModule module;

    public TimetableCommand(@Nonnull final TimetableModule module) {
        super(AccessLevel.GUILD_ONLY);
        this.module = module;

        // Add pattern manually in first position so it's not overridden by the day pattern
        patterns.add(0, new CommandPattern(
                new Argument[]{
                        Argument.forString("all")
                }, (e, a, o) -> onWeek(e, o)
        ));
    }

    @RegisterPattern
    public void onDefault(@Nonnull final CommandEvent event, @Nonnull final Options options) {
        final IUTGroup group = getGroup(event);
        if (group == null) {
            event.getChannel().sendMessage(MESSAGE_ERROR_NO_GROUP).queue();
            return;
        }

        // Get today date
        final Calendar date = Calendar.getInstance(TimeZone.getTimeZone("ECT"), Locale.FRANCE);
        boolean isToday = true;

        // If it's the weekend, take the next monday at midnight
        if (isWeekEnd(date)) {
            isToday = false;
            date.add(Calendar.WEEK_OF_YEAR, 1);
            date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else if (date.get(Calendar.HOUR_OF_DAY) >= 19) {
            isToday = false;
            date.add(Calendar.DAY_OF_WEEK, 1);
        }

        final List<Lesson> lessons = module.queryDayForGroup(group, date);
        lessons.sort(null); // Sort by date

        final EmbedBuilder builder = createEmbedForDay(group, lessons, date, options.has(DETAILS), isToday);

        event.getChannel().sendMessage(
                new MessageBuilder(MESSAGE_HEADER_WARNING).setEmbed(builder.build()).build()
        ).queue();
    }

    @RegisterPattern(arguments = "STRING")
    public void onDay(@Nonnull final CommandEvent event, @Nonnull final Options options, @Nonnull String day) {
        final IUTGroup group = getGroup(event);
        if (group == null) {
            event.getChannel().sendMessage(MESSAGE_ERROR_NO_GROUP).queue();
            return;
        }

        final Calendar date = Calendar.getInstance(TimeZone.getTimeZone("ECT"), Locale.FRANCE);
        if (isWeekEnd(date)) date.add(Calendar.WEEK_OF_YEAR, 1);

        day = day.toLowerCase();
        switch (day) {
            case "lundi":
                date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case "mardi":
                date.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                break;
            case "mercredi":
                date.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                break;
            case "jeudi":
                date.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                break;
            case "vendredi":
                date.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                break;
            case "samedi":
                date.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                break;
            default:
                event.getChannel().sendMessage(MESSAGE_ERROR_INVALID_DAY).queue();
                return;
        }

        final List<Lesson> lessons = module.queryDayForGroup(group, date);
        lessons.sort(null);

        final EmbedBuilder builder = createEmbedForDay(group, lessons, date, options.has(DETAILS), false);

        event.getChannel().sendMessage(
                new MessageBuilder(MESSAGE_HEADER_WARNING).setEmbed(builder.build()).build()
        ).queue();
    }

    private void onWeek(@Nonnull final CommandEvent event, @Nonnull final Options options) {
        final IUTGroup group = getGroup(event);
        if (group == null) {
            event.getChannel().sendMessage(MESSAGE_ERROR_NO_GROUP).queue();
            return;
        }

        final Calendar date = Calendar.getInstance(TimeZone.getTimeZone("ECT"), Locale.FRANCE);

        final Calendar target = (Calendar) date.clone();
        if (isWeekEnd(target)) target.add(Calendar.WEEK_OF_YEAR, 1);

        final List<MessageEmbed> embeds = new ArrayList<>();
        final boolean details = options.has(DETAILS);

        for (int i = Calendar.MONDAY; i < Calendar.SATURDAY; i++) {
            target.set(Calendar.DAY_OF_WEEK, i);
            final List<Lesson> lessons = module.queryDayForGroup(group, target);

            embeds.add(createEmbedForDay(group, lessons, target, details, false).build());
        }

        event.getChannel().sendMessage(MESSAGE_HEADER_WARNING).complete();
        for (MessageEmbed embed : embeds) {
            event.getChannel().sendTyping().complete();
            event.getChannel().sendMessage(embed).complete();
        }
    }

    @Nonnull
    private EmbedBuilder createEmbedForDay(@Nonnull final IUTGroup group, @Nonnull final List<Lesson> lessons, @Nonnull final Calendar date,
                                           final boolean details, final boolean isToday) {
        final EmbedBuilder builder = new EmbedBuilder()
                .setColor(group.getColor())
                .setAuthor(
                        String.format(MESSAGE_HEADER_DAY_FORMAT,
                                Utils.translateCalendarDay(date.get(Calendar.DAY_OF_WEEK)),
                                Utils.translateCalendarMonth(date.get(Calendar.MONTH)),
                                date.getTimeInMillis(),
                                group.getShortName()),
                        null, Icons.ICON_INFORMATION);

        if (lessons.size() == 0) {
            builder.setDescription("Aucun cours !");
            return builder;
        }

        if (isToday && lessons.get(lessons.size() - 1).getSchedule().getEnd() > date.getTimeInMillis()) {
            final long remainingMinutes = (lessons.get(lessons.size() - 1).getSchedule().getEnd() - date.getTimeInMillis()) / 60_000;
            builder.setFooter(String.format(MESSAGE_FOOTER_TIME_FORMAT,
                    remainingMinutes / 60, remainingMinutes % 60), Icons.ICON_BELL);
        }

        if (details) {
            // Print with details
            for (Lesson lesson : lessons) {
                String format = MESSAGE_LESSON_DETAIL_HEADER_FUTURE_FORMAT;

                if (isToday && lesson.getSchedule().getStart() < date.getTimeInMillis()) {
                    if (lesson.getSchedule().getEnd() < date.getTimeInMillis())
                        format = MESSAGE_LESSON_DETAIL_HEADER_DONE_FORMAT;
                    else format = MESSAGE_LESSON_DETAIL_HEADER_CURRENT_FORMAT;
                }

                builder.addField(
                        String.format(format,
                                lesson.getSchedule().getHourFormat(), lesson.getSchedule().getEndFormat(),
                                lesson.getInfo().getName(), lesson.getInfo().getRoom()),
                        String.format(MESSAGE_LESSON_DETAIL_CONTENT_FORMAT,
                                lesson.getSchedule().getDurationFormat(), lesson.getInfo().getCode(),
                                lesson.getInfo().getTeacher(), lesson.getGroupsFormat()),
                        false);
            }

        } else {
            // Print short version
            for (Lesson lesson : lessons) {
                String format = MESSAGE_LESSON_SIMPLE_FUTURE_FORMAT;

                // State in time of the lesson future/current/past
                if (isToday && lesson.getSchedule().getStart() < date.getTimeInMillis()) {
                    if (lesson.getSchedule().getEnd() < date.getTimeInMillis())
                        format = MESSAGE_LESSON_SIMPLE_DONE_FORMAT;
                    else format = MESSAGE_LESSON_SIMPLE_CURRENT_FORMAT;
                }

                builder.appendDescription(
                        String.format(format,
                                lesson.getSchedule().getHourFormat(), lesson.getSchedule().getEndFormat(), lesson.getSchedule().getDurationFormat(),
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
