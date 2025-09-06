package com.avalon.calendar;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AvalonCalendarPlugin extends JavaPlugin implements Listener {

    private CalendarManager calendar;
    private BossBar bossBar;
    private BukkitTask actionbarTask;
    private long prevDaytime = -1;
    private long lastAdvanceMs = 0L;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        this.calendar = new CalendarManager(this);

        if (getCommand("calendar") != null) {
            getCommand("calendar").setExecutor(new CalendarCommand(this));
        }
        getServer().getPluginManager().registerEvents(this, this);

        setupBossbar();
        startActionbar();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new CalendarPlaceholders(this, calendar).register();
        }

        getLogger().info("AvalonCalendar enabled.");
        Bukkit.getScheduler().runTaskLater(this, () ->
                Bukkit.broadcastMessage(color("&6[Avalon Calendar]&a Loaded. Use &e/calendar show")),
                40L);
    }

    @Override
    public void onDisable() {
        if (bossBar != null) bossBar.removeAll();
        if (actionbarTask != null) actionbarTask.cancel();
    }

    // Advance on vanilla/typical sleep skip
    @EventHandler
    public void onTimeSkip(TimeSkipEvent e) {
        if (e.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            advanceOneDayAndBroadcast();
        }
    }

    // Ensure new players see the bossbar
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (bossBar != null && getConfig().getBoolean("display.bossbar.enabled", true)) {
            bossBar.addPlayer(e.getPlayer());
        }
    }

    // ----- Displays -----
    public void setupBossbar() {
        if (!getConfig().getBoolean("display.bossbar.enabled", true)) return;
        BarColor color = BarColor.valueOf(getConfig().getString("display.bossbar.color", "GREEN"));
        BarStyle style = BarStyle.valueOf(getConfig().getString("display.bossbar.style", "SEGMENTED_10"));
        String title = applyPlaceholders(getConfig().getString("display.bossbar.title", "%MONTH_FULL% %DAY%, %YEAR% — %PROGRESS%%%"));
        bossBar = Bukkit.createBossBar(color(title), color, style);
        bossBar.setProgress(progressToSunrise());
        for (Player p : Bukkit.getOnlinePlayers()) bossBar.addPlayer(p);
    }

    public void updateBossbar() {
        if (bossBar == null) return;
        String title = applyPlaceholders(getConfig().getString("display.bossbar.title", "%MONTH_FULL% %DAY%, %YEAR% — %PROGRESS%%%"));
        bossBar.setTitle(color(title));
        bossBar.setProgress(progressToSunrise());
    }

    private void startActionbar() {
        if (!getConfig().getBoolean("display.actionbar.enabled", true)) return;
        int interval = Math.max(20, getConfig().getInt("display.actionbar.intervalTicks", 40));
        actionbarTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            World w = Bukkit.getWorlds().get(0);
            if (w != null) {
                long t = w.getFullTime() % 24000L;
                // Sunrise fallback if plugins skip night without NIGHT_SKIP
                boolean fallback = getConfig().getBoolean("calendar.sunriseFallback", true);
                if (fallback && prevDaytime >= 23000 && t <= 200) {
                    long now = System.currentTimeMillis();
                    if (now - lastAdvanceMs > 5000) {
                        advanceOneDayAndBroadcast();
                        lastAdvanceMs = now;
                    }
                }
                prevDaytime = t;
            }
            String fmt = applyPlaceholders(getConfig().getString("display.actionbar.format",
                    "%MONTH_ABBR% %DAY%, %YEAR% — %PROGRESS%%%"));
            String msg = color(fmt);
            for (Player p : Bukkit.getOnlinePlayers()) p.sendActionBar(msg);
            updateBossbar();
        }, 40L, interval);
    }

    // ----- Calendar ops -----
    private void advanceOneDayAndBroadcast() {
        calendar.advanceOneDay();
        if (getConfig().getBoolean("broadcast.onNewDate", true)) {
            String msg = applyPlaceholders(getConfig().getString("broadcast.format", "&6☀ New Date: %WEEKDAY%, %MONTH_FULL% %DAY%, %YEAR%"));
            Bukkit.broadcastMessage(color(msg));
            try {
                Sound s = Sound.valueOf(getConfig().getString("broadcast.sound", "ENTITY_PLAYER_LEVELUP"));
                for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), s, 0.8f, 1.2f);
            } catch (IllegalArgumentException ignored) {}
        }
        updateBossbar();
    }

    // ----- Helpers -----
    public double progressToSunrise() {
        World w = Bukkit.getWorlds().get(0);
        if (w == null) return 0.0;
        long t = w.getFullTime() % 24000L;
        return Math.min(1.0, Math.max(0.0, (double) t / 24000.0));
    }

    public String applyPlaceholders(String raw) {
        if (raw == null) return "";
        raw = raw.replace("%YEAR%", String.valueOf(calendar.getYear()))
                .replace("%MONTH%", String.valueOf(calendar.getMonth()))
                .replace("%DAY%", String.valueOf(calendar.getDay()))
                .replace("%MONTH_FULL%", calendar.monthFull())
                .replace("%MONTH_ABBR%", calendar.monthAbbr())
                .replace("%WEEKDAY%", calendar.weekdayName())
                .replace("%PROGRESS%", String.valueOf((int)Math.round(progressToSunrise()*100)));
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            raw = PlaceholderAPI.setPlaceholders(null, raw);
        }
        return raw;
    }

    public String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public CalendarManager getCalendar() { return calendar; }
    public BossBar getBossBar() { return bossBar; }

    public void reloadAll() {
        reloadConfig();
        if (bossBar != null) { bossBar.removeAll(); bossBar = null; }
        setupBossbar();
        updateBossbar();
    }
}
