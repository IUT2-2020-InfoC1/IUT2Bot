package com.jesus_crie.iut2_bot.timetable;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.iut2_bot.timetable.TimetableExtractor.Lesson;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot_night_config_wrapper.NightConfigWrapperModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class TimetableModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("Timetable", "Jesus_Crie", "", "1.0", 1);

    private static final Logger LOG = LoggerFactory.getLogger("Timetable");

    private static final String CONFIG_TIMETABLE = "timetable";
    private static final String CONFIG_ROOT = "data";
    private static final String CONFIG_MODULE_CODE = "module";
    private static final String CONFIG_MODULE_NAME = "module_name";
    private static final String CONFIG_ROOM = "room";
    private static final String CONFIG_TEACHER = "teacher";
    private static final String CONFIG_TIME_START = "start_millis";
    private static final String CONFIG_TIME_END = "end_millis";
    private static final String CONFIG_GROUPS = "groups";

    private Config config;
    private final List<Lesson> lessons = new CopyOnWriteArrayList<>();

    public TimetableModule() {
        super(INFO);
    }

    @Override
    public void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull final ModularBotBuilder builder) {
        // Init timetable data
        final NightConfigWrapperModule configModule = moduleManager.getModule(NightConfigWrapperModule.class);
        assert configModule != null; // Yolo
        configModule.useSecondaryConfig(CONFIG_TIMETABLE, "timetable.json");

        config = configModule.getSecondaryConfig(CONFIG_TIMETABLE);
    }

    @Override
    public void onPostInitialization() {
        // Load timetable data
        final List<Config> root = config.get(CONFIG_ROOT);

        for (Config lessonNode : root) {

            lessons.add(
                    new Lesson(
                            lessonNode.get(CONFIG_MODULE_CODE),
                            lessonNode.get(CONFIG_MODULE_NAME),
                            lessonNode.get(CONFIG_TIME_START),
                            lessonNode.get(CONFIG_TIME_END),
                            lessonNode.get(CONFIG_ROOM),
                            lessonNode.get(CONFIG_TEACHER),
                            lessonNode.get(CONFIG_GROUPS)
                    )
            );
        }
    }

    @Nonnull
    public List<Lesson> queryDay(@Nonnull final Calendar day) {

        return lessons.stream()
                .filter(l -> l.getSchedule().getDay().get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR))
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<Lesson> queryGroup(@Nonnull final IUTGroup group) {
        if (group == IUTGroup.NONE)
            throw new IllegalArgumentException();

        return lessons.stream()
                .filter(l -> l.getGroups().contains(group))
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<Lesson> queryDayForGroup(@Nonnull final IUTGroup group, @Nonnull final Calendar day) {
        if (group == IUTGroup.NONE)
            throw new IllegalArgumentException();

        return queryDay(day).stream()
                .filter(l -> l.getGroups().contains(group))
                .collect(Collectors.toList());
    }

    private Calendar cleanCalendar(@Nonnull final Calendar calendar) {
        return new Calendar.Builder()
                .set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                .set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                .set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                .build();
    }
}
