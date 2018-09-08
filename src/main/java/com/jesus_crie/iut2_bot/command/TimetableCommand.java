package com.jesus_crie.iut2_bot.command;

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
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.List;

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

    private static MessageEmbed MESSAGE_ERROR_NO_GROUP = Utils.getErrorMessage("Vous n'êtes dans aucun groupe !", null);

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

        final Calendar today = Calendar.getInstance();
        if (isWeekEnd(today)) {
            today.add(Calendar.WEEK_OF_YEAR, 1);
            today.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }

        final List<Lesson> lessons = module.queryDayForGroup(group, today);
        event.fastReply(lessons.toString());

        // TODO 09/09/18 Format this and send it
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
