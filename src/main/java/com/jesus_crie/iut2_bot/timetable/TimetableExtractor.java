package com.jesus_crie.iut2_bot.timetable;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimetableExtractor {

    private static final Logger LOG = LoggerFactory.getLogger("Timetable Extractor");

    private static final int WEEK_OFFSET = 17;

    private static final String W_MAIN_FORMAT = "http://www-ade.iut2.upmf-grenoble.fr/ade/custom/modules/plannings/direct_planning.jsp?" +
            "resources=1849" +
            "&weeks=%s" +
            "&showTree=true" +
            "&showPianoDays=true" +
            "&login=WebINFO&password=MPINFO" +
            "&projectId=11" +
            "&displayConfName=Vue_Web_INFO_Etudiant" +
            "&showOptions=true" +
            "&showPianoWeeks=true" +
            "&days=0,1,2,3,4";

    private static final String W_SETTINGS = "http://www-ade.iut2.upmf-grenoble.fr/ade/custom/modules/plannings/appletparams.jsp?showMenu=false&showTree=true";
    private static final String W_CSS_TOGGLE_FORMAT = "input[name=%s]";
    private static final String[] W_CSS_TOGGLES = new String[]{
            "showTab", "showImage",
            "showTabWeek", "showTabDay", "showTabStage", "showTabDate", "aUrl", "showTabDuration"
    };

    private static final String W_TABLE_VIEW = "http://www-ade.iut2.upmf-grenoble.fr/ade/custom/modules/plannings/info.jsp?order=slot";
    private static final String W_JS_REQUEST_SELECT_S2 = "var req = new XMLHttpRequest();\n" +
            "req.open('GET'," +
            "'http://www-ade.iut2.upmf-grenoble.fr/ade/standard/gui/tree.jsp?" +
            "selectBranchId=1469" +
            "&reset=false" +
            "&forceLoad=false" +
            "&scroll=0'," +
            "false);\n" +
            "req.send(null);";

    private static final String W_CSS_SELECTOR_TABLE = "tr:not([class])";

    private static final SimpleDateFormat P_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat P_HOUR_FORMAT = new SimpleDateFormat("HH'h'mm");
    private static final Pattern P_DURATION_REGEX = Pattern.compile("([0-9]+)h(?>([0-9]+)min)?");

    private ChromeDriver driver;

    public TimetableExtractor() {
        this("/usr/bin/chromedriver");
    }

    public TimetableExtractor(@Nonnull final String chromeDriverPath) {
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
    }

    public void setupDriver(@Nonnull final String... additionalOptions) {
        LOG.info("Setting up driver...");
        final ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--silent");
        if (additionalOptions.length != 0)
            options.addArguments(additionalOptions);

        driver = new ChromeDriver(options);
    }

    public void unloadDriver() {
        LOG.info("Destroying driver...");
        driver.close();
        driver.quit();
        driver = null;
    }

    @Nonnull
    public List<Config> query(final int amountWeeks) {
        if (driver == null)
            throw new IllegalStateException("The driver hasn't been initialized !");
        else if (amountWeeks <= 0)
            throw new IllegalArgumentException("The amount of weeks must be >= 0 !");

        // Compute week numbers
        final int currentWeek = Calendar.getInstance(Locale.FRANCE).get(Calendar.WEEK_OF_YEAR) + WEEK_OFFSET;
        final String weekFormatted;
        if (amountWeeks == 1)
            weekFormatted = String.valueOf(currentWeek);
        else {
            weekFormatted = IntStream.range(currentWeek, currentWeek + amountWeeks)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(","));
        }
        LOG.info("Querying from week " + currentWeek + " to week " + (currentWeek + amountWeeks - 1));

        /* Query time */

        // Main page
        LOG.info("Query: Main page to initiate session...");
        driver.get(String.format(W_MAIN_FORMAT, weekFormatted));

        LOG.info("Query: Setting page...");
        driver.get(W_SETTINGS);
        LOG.info("Settings: Toggle options...");
        final WebElement form = driver.findElement(By.tagName("form"));
        for (String toggle : W_CSS_TOGGLES)
            form.findElement(By.cssSelector(String.format(W_CSS_TOGGLE_FORMAT, toggle))).click();
        LOG.info("Settings: Submitting form...");
        form.submit();

        LOG.info("Query: Table view");
        driver.get(W_TABLE_VIEW);
        LOG.info("Table: XMLHttpRequest select all S2");
        driver.executeScript(W_JS_REQUEST_SELECT_S2);
        LOG.info("Query: Refresh table view");
        driver.navigate().refresh();
        LOG.info("Query: All done !");

        LOG.info("Start processing table...");

        return driver.findElements(By.cssSelector(W_CSS_SELECTOR_TABLE)).stream()
                .map(node -> {
                            final Config extracted = Config.inMemory();
                            extracted.set("date", node.findElement(By.cssSelector("td:nth-child(1)")).getText());
                            extracted.set("module", node.findElement(By.cssSelector("td:nth-child(2)")).getText());
                            extracted.set("hour", node.findElement(By.cssSelector("td:nth-child(3)")).getText());
                            extracted.set("duration", node.findElement(By.cssSelector("td:nth-child(4)")).getText());
                            extracted.set("name", node.findElement(By.cssSelector("td:nth-child(5)")).getText());
                            extracted.set("groups", node.findElement(By.cssSelector("td:nth-child(6)")).getText());
                            extracted.set("teacher", node.findElement(By.cssSelector("td:nth-child(7)")).getText());
                            extracted.set("room", node.findElement(By.cssSelector("td:nth-child(8)")).getText());

                            return extracted;
                        }
                ).collect(Collectors.toList());
    }

    @Nonnull
    public List<Lesson> processData(@Nonnull final List<Config> data) {
        LOG.info("Processing data...");
        return data.stream()
                .map(raw -> {
                    try {
                        long start = P_DATE_FORMAT.parse(raw.get("date")).getTime();

                        start += P_HOUR_FORMAT.parse(raw.get("hour")).getTime();

                        final Duration duration = parseDuration(raw.get("duration"));

                        final long end = start + duration.toMillis();

                        return new Lesson(
                                raw.get("module"),
                                raw.get("name"),
                                start,
                                end,
                                raw.get("room"),
                                raw.get("teacher"),
                                Arrays.stream(raw.<String>get("groups").split(" "))
                                        .map(IUTGroup::fromName)
                                        .collect(Collectors.toList())
                        );
                    } catch (ParseException e) {
                        LOG.warn("Failed to parse a lesson, skipping...");
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nonnull
    private Duration parseDuration(@Nonnull final String raw) {
        final Matcher m = P_DURATION_REGEX.matcher(raw);
        if (m.matches())
            return Duration.ofHours(Long.parseLong(m.group(1)))
                    .plusMinutes(m.groupCount() == 3 ? Long.parseLong(m.group(2)) : 0);

        return Duration.ZERO;
    }

    public static class Lesson implements Comparable<Lesson> {

        private final String module;
        private final String moduleName;
        private final long startM;
        private final long endM;
        private final String room;
        private final String teacher;
        private final List<IUTGroup> groups;

        /**
         * @param module     - The code of the module.
         * @param moduleName - The name of the module.
         * @param startM     - The timestamp of the start of the lesson.
         * @param endM       - The timestamp of the end of the lesson.
         * @param room       - The name of the room.
         * @param teacher    - The teacher of the lesson.
         * @param groups     - The groups attending to this lesson.
         */
        public Lesson(@Nonnull final String module,
                      @Nonnull final String moduleName,
                      final long startM,
                      final long endM,
                      @Nonnull final String room,
                      @Nonnull final String teacher,
                      @Nonnull final List<IUTGroup> groups) {
            this.module = module;
            this.moduleName = moduleName;
            this.startM = startM;
            this.endM = endM;
            this.room = room;
            this.teacher = teacher;
            this.groups = groups;
        }

        @Nonnull
        public String getModule() {
            return module;
        }

        @Nonnull
        public String getModuleName() {
            return moduleName;
        }

        public long getStartMillis() {
            return startM;
        }

        public long getEndMillis() {
            return endM;
        }

        public long getDurationMillis() {
            return endM - startM - 3_600_000; // Minus 1h
        }

        @Nonnull
        public String getRoom() {
            return room;
        }

        @Nonnull
        public String getTeacher() {
            return teacher;
        }

        @Nonnull
        public List<IUTGroup> getGroups() {
            return groups;
        }

        @Override
        public int compareTo(@NotNull final TimetableExtractor.Lesson o) {
            return (int) (startM - o.startM);
        }
    }
}
