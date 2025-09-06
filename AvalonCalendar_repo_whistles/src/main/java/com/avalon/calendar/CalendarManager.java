package com.avalon.calendar;

import org.bukkit.configuration.file.FileConfiguration;

public class CalendarManager {

    private final AvalonCalendarPlugin plugin;

    private int year;
    private int month;
    private int day;
    private int weekdayIndex; // 0=Mon..6=Sun

    private final String[] monthsFull;
    private final String[] monthsAbbr;
    private final String[] weekdays = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

    public CalendarManager(AvalonCalendarPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration cfg = plugin.getConfig();
        this.year = cfg.getInt("storage.year", cfg.getInt("start.year", 2025));
        this.month = cfg.getInt("storage.month", cfg.getInt("start.month", 1));
        this.day = cfg.getInt("storage.day", cfg.getInt("start.day", 1));
        this.weekdayIndex = cfg.getInt("calendar.weekdayIndex", 2); // Jan 1, 2025 is Wed (2)
        this.monthsFull = cfg.getStringList("monthsFull").toArray(new String[0]);
        this.monthsAbbr = cfg.getStringList("monthsAbbr").toArray(new String[0]);
        if (monthsFull.length != 12 || monthsAbbr.length != 12) {
            throw new IllegalStateException("monthsFull/monthsAbbr must have 12 entries!");
        }
        normalize();
        persist();
    }

    public void advanceOneDay() {
        weekdayIndex = (weekdayIndex + 1) % 7;
        day++;
        int ml = monthLength(year, month);
        if (day > ml) {
            day = 1;
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
        }
        persist();
    }

    public void setDate(int y, int m, int d) {
        this.year = y; this.month = Math.max(1, Math.min(12, m));
        this.day = Math.max(1, Math.min(monthLength(y, m), d));
        persist();
    }

    public void resetToStart() {
        FileConfiguration cfg = plugin.getConfig();
        this.year = cfg.getInt("start.year", 2025);
        this.month = cfg.getInt("start.month", 1);
        this.day = cfg.getInt("start.day", 1);
        this.weekdayIndex = cfg.getInt("calendar.weekdayIndex", 2);
        persist();
    }

    private void persist() {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("storage.year", year);
        cfg.set("storage.month", month);
        cfg.set("storage.day", day);
        plugin.saveConfig();
    }

    private void normalize() {
        if (month < 1) month = 1;
        if (month > 12) month = 12;
        int ml = monthLength(year, month);
        if (day < 1) day = 1;
        if (day > ml) day = ml;
        weekdayIndex = ((weekdayIndex % 7) + 7) % 7;
    }

    public boolean isLeap(int y) {
        return (y % 400 == 0) || ((y % 4 == 0) && (y % 100 != 0));
    }

    public int monthLength(int y, int m) {
        return switch (m) {
            case 1,3,5,7,8,10,12 -> 31;
            case 4,6,9,11 -> 30;
            case 2 -> isLeap(y) ? 29 : 28;
            default -> 30;
        };
    }

    public String monthFull() { return monthsFull[Math.max(0, Math.min(11, month-1))]; }
    public String monthAbbr() { return monthsAbbr[Math.max(0, Math.min(11, month-1))]; }
    public String weekdayName() { return weekdays[Math.max(0, Math.min(6, weekdayIndex))]; }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public int getWeekdayIndex() { return weekdayIndex; }
}
