package com.jesus_crie.iut2_bot.timetable;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot_night_config_wrapper.NightConfigWrapperModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimetableModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("Timetable", "Jesus_Crie", "", "1.0", 1);

    private static final Logger LOG = LoggerFactory.getLogger("Timetable");

    private static final String CONFIG_TIMETABLE = "timetable";
    private static final String CONFIG_ROOT = "data";
    private static final String CONFIG_CLASS_ROOT = "class";
    private static final String CONFIG_CLASS_CODE = "code";
    private static final String CONFIG_CLASS_NAME = "name";
    private static final String CONFIG_CLASS_ROOM = "room";
    private static final String CONFIG_CLASS_TEACHER = "teacher";
    private static final String CONFIG_DATE_ROOT = "date";
    private static final String CONFIG_DATE_DAY = "day";
    private static final String CONFIG_DATE_DURATION = "duration";
    private static final String CONFIG_DATE_HOUR = "hour";
    private static final String CONFIG_GROUPS = "groups";

    private Config config;
    private final List<Lesson> lessons = new CopyOnWriteArrayList<>();

    public TimetableModule() {
        super(INFO);
    }

    @Override
    public void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull final ModularBotBuilder builder) {
        final NightConfigWrapperModule configModule = moduleManager.getModule(NightConfigWrapperModule.class);
        assert configModule != null; // Yolo
        configModule.useSecondaryConfig(CONFIG_TIMETABLE, "timetable.json");

        config = configModule.getSecondaryConfig(CONFIG_TIMETABLE);
    }

    @Override
    public void onPostInitialization() {
        final List<Config> root = config.get(CONFIG_ROOT);

        for (Config lessonNode : root) {
            final Lesson.ClassInfo info = new Lesson.ClassInfo(
                    lessonNode.get(CONFIG_CLASS_ROOT + "." + CONFIG_CLASS_CODE),
                    lessonNode.get(CONFIG_CLASS_ROOT + "." + CONFIG_CLASS_NAME),
                    lessonNode.get(CONFIG_CLASS_ROOT + "." + CONFIG_CLASS_ROOM),
                    lessonNode.get(CONFIG_CLASS_ROOT + "." + CONFIG_CLASS_TEACHER)
            );

            final Lesson.Time time = new Lesson.Time(
                    new Date(lessonNode.getLong(CONFIG_DATE_ROOT + "." + CONFIG_DATE_DAY) * 1000),
                    Duration.ofMinutes(lessonNode.getInt(CONFIG_DATE_ROOT + "." + CONFIG_DATE_DURATION)),
                    Duration.ofMinutes(lessonNode.getInt(CONFIG_DATE_ROOT + "." + CONFIG_DATE_HOUR))
            );

            final Lesson lesson = new Lesson(info, time, lessonNode.get(CONFIG_GROUPS));
            lessons.add(lesson);
        }

        lessons.forEach(System.out::println);
    }
}
