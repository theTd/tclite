package com.mineclay.tclite;

public class LocalizationUtil {
    public static String localizeTime(long second) {
        if (second < 0) second = 0;
        if (second == 0) return "0\u79D2";

        Time time = new Time(second);

        int lvl = 0;

        StringBuilder sb = new StringBuilder();
        if (time.years != 0) {
            sb.append(time.years).append("\u5E74");
            lvl = 4;
        }
        if (time.days != 0) {
            sb.append(time.days).append("\u5929");
            lvl = Math.max(lvl, 3);
        }
        if (lvl < 4 && time.hours != 0) {
            sb.append(time.hours).append("\u65F6");
            lvl = Math.max(lvl, 2);
        }
        if (lvl < 3 && time.minutes != 0) {
            sb.append(time.minutes).append("\u5206");
            lvl = Math.max(lvl, 1);
        }
        if (lvl < 2 && time.seconds != 0)
            sb.append(time.seconds).append("\u79D2");

        if (lvl >= 2) time.removeSeconds();
        if (lvl >= 3) time.removeMinutes();
        if (lvl == 4) time.removeHours();

        if (sb.charAt(sb.length() - 1) == '\u5206' && lvl == 1) {
            sb.append('\u949F');
        }
        if (sb.charAt(sb.length() - 1) == '\u65F6') {
            sb.insert(sb.length() - 1, "\u5C0F");
        }
        return sb.toString();
    }

    private static class Time {
        long years, days, hours, minutes, seconds;

        Time(long second) {
            years = second / 31536000;
            second -= years * 31536000;
            days = second / 86400;
            second -= days * 86400;
            hours = second / 3600;
            second -= hours * 3600;
            minutes = second / 60;
            second -= minutes * 60;
            seconds = second;
        }

        void addSeconds() {
            if (++seconds == 60) {
                addMinute();
                seconds = 0;
            }
        }

        void removeSeconds() {
            if (seconds >= 30) addMinute();
            seconds = 0;
        }

        void addMinute() {
            if (++minutes == 60) {
                minutes = 0;
                addHour();
            }
        }

        void removeMinutes() {
            removeSeconds();
            if (minutes >= 30) addHour();
            minutes = 0;
        }

        void addHour() {
            if (++hours == 24) {
                hours = 0;
                addDay();
            }
        }

        void removeHours() {
            removeMinutes();
            if (hours >= 12) addDay();
            hours = 0;
        }

        void addDay() {
            if (++days == 365) {
                days = 0;
                addYear();
            }
        }

        void removeDays() {
            removeHours();
            if (days >= 182) addYear();
            days = 0;
        }

        void addYear() {
            years++;
        }
    }
}
