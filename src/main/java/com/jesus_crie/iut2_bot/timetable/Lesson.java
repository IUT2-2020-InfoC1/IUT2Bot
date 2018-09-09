package com.jesus_crie.iut2_bot.timetable;

import com.jesus_crie.iut2_bot.Utils;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class Lesson implements Comparable<Lesson> {

    private static final SimpleDateFormat FORMAT_DAY = new SimpleDateFormat("dd/MM/yyyy");
    private static final String FORMAT_HOUR = "%1$02dh%2$02d";
    private static final String FORMAT_DURATION = "%1$dh%2$02d";

    private final ClassInfo info;
    private final Time time;
    private final List<IUTGroup> groups;

    public Lesson(@Nonnull final ClassInfo info, @Nonnull final Time time, @Nonnull final List<String> groups) {
        this.info = info;
        this.time = time;
        this.groups = groups.stream()
                .map(IUTGroup::fromName)
                .collect(Collectors.toList());
    }

    public ClassInfo getInfo() {
        return info;
    }

    public Time getTime() {
        return time;
    }

    public List<IUTGroup> getGroups() {
        return groups;
    }

    public String getGroupsFormat() {
        return groups.stream()
                .map(IUTGroup::getShortName)
                .collect(Collectors.joining(", "));
    }

    public String getId() {
        return String.valueOf(time.day.toInstant().getEpochSecond()) + String.valueOf(time.hour.toMinutes()) + info.room;
    }

    public boolean isGroupPresent(@Nonnull final String group) {
        return group.contains(group);
    }

    @Override
    public String toString() {
        return "[\n\t" + info + ",\n\t" + time + ",\n\t" + groups + "\n]";
    }

    @Override
    public int compareTo(@Nonnull final Lesson o) {
        return time.day.compareTo(o.time.day);
    }

    public static class ClassInfo {

        private final String code;
        private final String name;
        private final String room;
        private final String teacher;

        public ClassInfo(@Nonnull final String code, @Nonnull final String name, @Nonnull final String room,
                         @Nonnull final String teacher) {
            this.code = code;
            this.name = name;
            this.room = room;
            this.teacher = teacher;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getRoom() {
            return room;
        }

        public String getTeacher() {
            return teacher;
        }

        @Override
        public String toString() {
            return "[" + code + ": " + name + ", " + room + ", " + teacher + "]";
        }
    }

    public static class Time {
        private final Calendar day;
        private final Duration duration;
        private final Duration hour;

        public Time(final long day, @Nonnull final Duration duration, @Nonnull final Duration hour) {
            this.day = Calendar.getInstance();
            this.day.setTimeInMillis(day);
            this.duration = duration;
            this.hour = hour;
        }

        public Calendar getDay() {
            return day;
        }

        public String getDayFormat() {
            return FORMAT_DAY.format(day.getTime());
        }

        public Duration getDuration() {
            return duration;
        }

        public String getDurationFormat() {
            return String.format(FORMAT_DURATION, duration.toHours(), duration.toMinutes() % 60);
        }

        public Duration getHour() {
            return hour;
        }

        public String getHourFormat() {
            return String.format(FORMAT_HOUR, hour.toHours(), hour.toMinutes() % 60);
        }

        public String getEndFormat() {
            final Duration end = hour.plus(duration);
            return String.format(FORMAT_HOUR, end.toHours(), hour.toMinutes() % 60);
        }

        public long getStart() {
            return day.toInstant().toEpochMilli() + hour.toMillis();
        }

        public long getEnd() {
            return day.toInstant().toEpochMilli() + hour.plus(duration).toMillis();
        }

        @Override
        public String toString() {
            return "[" + Utils.translateCalendarDay(day.get(Calendar.DAY_OF_WEEK)) + " " + getDayFormat() +
                    " at " + getHourFormat() + " during " + getDurationFormat() + "]";
        }
    }
}
