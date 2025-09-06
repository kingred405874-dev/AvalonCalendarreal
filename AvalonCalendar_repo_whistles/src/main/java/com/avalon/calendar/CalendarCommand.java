package com.avalon.calendar;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CalendarCommand implements CommandExecutor {

    private final AvalonCalendarPlugin plugin;

    public CalendarCommand(AvalonCalendarPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("show")) {
            sender.sendMessage(ChatColor.GOLD + "Date: " + ChatColor.YELLOW
                    + plugin.getCalendar().monthFull() + " "
                    + plugin.getCalendar().getDay() + ", "
                    + plugin.getCalendar().getYear());
            return true;
        }

        if (args[0].equalsIgnoreCase("togglebossbar")) {
            boolean was = plugin.getConfig().getBoolean("display.bossbar.enabled", true);
            plugin.getConfig().set("display.bossbar.enabled", !was);
            plugin.saveConfig();
            plugin.reloadAll();
            sender.sendMessage(ChatColor.GREEN+"Bossbar " + (!was ? "enabled" : "disabled"));
            return true;
        }

        if (args[0].equalsIgnoreCase("toggleactionbar")) {
            boolean was = plugin.getConfig().getBoolean("display.actionbar.enabled", true);
            plugin.getConfig().set("display.actionbar.enabled", !was);
            plugin.saveConfig();
            plugin.reloadAll();
            sender.sendMessage(ChatColor.GREEN+"Actionbar " + (!was ? "enabled" : "disabled"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            plugin.getCalendar().resetToStart();
            sender.sendMessage(ChatColor.GREEN+"Date reset.");
            plugin.reloadAll();
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 4) { sender.sendMessage(ChatColor.RED+"Usage: /calendar set <year> <month> <day>"); return true; }
            try {
                int y = Integer.parseInt(args[1]);
                int m = Integer.parseInt(args[2]);
                int d = Integer.parseInt(args[3]);
                plugin.getCalendar().setDate(y, m, d);
                sender.sendMessage(ChatColor.GREEN+"Date set to: "+plugin.getCalendar().monthFull()+" "+plugin.getCalendar().getDay()+", "+plugin.getCalendar().getYear());
                plugin.reloadAll();
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED+"Invalid numbers.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadAll();
            sender.sendMessage(ChatColor.GREEN+"AvalonCalendar reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.RED+"Usage: /"+label+" [show|togglebossbar|toggleactionbar|reset|set|reload]");
        return true;
    }
}
