package com.avalon.calendar;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class CalendarPlaceholders extends PlaceholderExpansion {

    private final AvalonCalendarPlugin plugin;
    private final CalendarManager cal;

    public CalendarPlaceholders(AvalonCalendarPlugin plugin, CalendarManager cal) {
        this.plugin = plugin; this.cal = cal;
    }

    @Override public @NotNull String getIdentifier() { return "avaloncalendar"; }
    @Override public @NotNull String getAuthor() { return "Avalon"; }
    @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return switch (params.toLowerCase()) {
            case "year" -> String.valueOf(cal.getYear());
            case "month" -> String.valueOf(cal.getMonth());
            case "day" -> String.valueOf(cal.getDay());
            case "date" -> cal.monthFull() + " " + cal.getDay() + ", " + cal.getYear();
            default -> null;
        };
    }
}
