package com.jesus_crie.iut2_bot.timetable;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
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

    private static final long THRESHOLD_CACHE_EXPIRED_MILLIS = 172_800_000; // 48 hours

    private static final String CONFIG_TIMETABLE = "timetable";
    private static final String CONFIG_ROOT = "data";
    private static final String CONFIG_LAST_UPDATE = "last_update";
    private static final String CONFIG_MODULE_CODE = "module";
    private static final String CONFIG_MODULE_NAME = "module_name";
    private static final String CONFIG_ROOM = "room";
    private static final String CONFIG_TEACHER = "teacher";
    private static final String CONFIG_TIME_START = "start_millis";
    private static final String CONFIG_TIME_END = "end_millis";
    private static final String CONFIG_GROUPS = "groups";

    private FileConfig config;
    private final List<TimetableExtractor.Lesson> lessons = new CopyOnWriteArrayList<>();
    private TimetableExtractor extractor;

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
        reloadData();
    }

    @Override
    public void onUnload() {
        saveData();
    }

    private boolean isCacheExpired() {
        final long lastUpdate = config.getLong(CONFIG_LAST_UPDATE);
        // If cache is older that threshold, re-query
        return lastUpdate < System.currentTimeMillis() - THRESHOLD_CACHE_EXPIRED_MILLIS;
    }

    private void invalidateCache() {
        LOG.info("Invalidating timetable cache...");
        extractNextWeek();
        saveData();
        LOG.info("Cache replenished !");
    }

    public void reloadData() {
        LOG.info("Re/Loading timetable data...");

        // Load timetable data
        config.load();

        // Check expired
        if (isCacheExpired()) {
            invalidateCache();
            return;
        }

        final List<Config> root = config.get(CONFIG_ROOT);

        LOG.info("Processing timetable data...");

        lessons.clear();
        root.parallelStream()
                .map(node ->
                        new TimetableExtractor.Lesson(
                                node.get(CONFIG_MODULE_CODE),
                                node.get(CONFIG_MODULE_NAME),
                                node.get(CONFIG_TIME_START),
                                node.get(CONFIG_TIME_END),
                                node.get(CONFIG_ROOM),
                                node.get(CONFIG_TEACHER),
                                node.<List<String>>get(CONFIG_GROUPS).stream()
                                        .map(IUTGroup::fromName)
                                        .collect(Collectors.toList()
                                        )
                        )
                ).forEach(lessons::add);
    }

    private void saveData() {
        LOG.info("Saving timetable data...");
        // Process lessons
        final List<Config> dataNodes = lessons.parallelStream()
                .map(l -> {
                    final Config node = config.createSubConfig();

                    node.set(CONFIG_MODULE_CODE, l.getModule());
                    node.set(CONFIG_MODULE_NAME, l.getModuleName());
                    node.set(CONFIG_TIME_START, l.getStartMillis());
                    node.set(CONFIG_TIME_END, l.getEndMillis());
                    node.set(CONFIG_ROOM, l.getRoom());
                    node.set(CONFIG_TEACHER, l.getTeacher());
                    node.set(CONFIG_GROUPS, l.getGroups().stream()
                            .map(IUTGroup::getName)
                            .collect(Collectors.toList()));

                    return node;
                }).collect(Collectors.toList());

        config.set(CONFIG_ROOT, dataNodes);
        config.set(CONFIG_LAST_UPDATE, System.currentTimeMillis());
        config.save();
    }

    public void extractNextWeek() {
        extractWeeks(1);
    }

    public void extractWeeks(final int amount) {
        if (extractor == null) {
            LOG.info("Spawning an extractor...");
            extractor = new TimetableExtractor();
            extractor.setupDriver();
        }

        LOG.info("Extracting " + amount + " weeks from ADE...");
        final List<TimetableExtractor.Lesson> lessons = extractor.processData(extractor.query(amount));

        extractor.unloadDriver();

        this.lessons.clear();
        this.lessons.addAll(lessons);
    }

    @Nonnull
    public List<TimetableExtractor.Lesson> queryDay(@Nonnull Calendar day) {

        if (isCacheExpired()) {
            invalidateCache();
        }

        day = cleanCalendar(day);

        final long lowerBound = day.getTimeInMillis();
        day.add(Calendar.DAY_OF_YEAR, 1);
        final long higherBound = day.getTimeInMillis();

        return lessons.stream()
                .filter(l -> l.getStartMillis() <= higherBound && l.getStartMillis() >= lowerBound)
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<TimetableExtractor.Lesson> queryGroup(@Nonnull final IUTGroup group) {
        if (group == IUTGroup.NONE)
            throw new IllegalArgumentException();

        return lessons.stream()
                .filter(l -> l.getGroups().contains(group))
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<TimetableExtractor.Lesson> queryDayForGroup(@Nonnull final IUTGroup group, @Nonnull final Calendar day) {
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
