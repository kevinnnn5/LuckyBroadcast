package com.rnxp.luckybroadcast;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LuckyBroadcast extends JavaPlugin implements CommandExecutor {

    private String broadcastPrefix;
    private String systemPrefix;
    private BukkitRunnable maintenanceCountdownTask;
    private BukkitRunnable restartingCountdownTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadPrefixes();

        getCommand("maintenance").setExecutor(this);
        getCommand("restarting").setExecutor(this);
        getCommand("bc").setExecutor(this);

        getLogger().info("LuckyBroadcast successfully enabled.");
    }

    private void loadPrefixes() {
        FileConfiguration config = getConfig();
        broadcastPrefix = ChatColor.translateAlternateColorCodes('&',
                config.getString("broadcast-prefix", "&e&lALERT&a / ] "));
        systemPrefix = ChatColor.translateAlternateColorCodes('&',
                config.getString("system-prefix", "&e&lSYSTEM&a /  "));
    }

    @Override
    public void onDisable() {
        if (maintenanceCountdownTask != null) {
            maintenanceCountdownTask.cancel();
        }
        if (restartingCountdownTask != null) {
            restartingCountdownTask.cancel();
        }
        getLogger().info("LuckyBroadcast has been disabled.");
    }
    private boolean hasPermission(CommandSender sender, String perm) {
        return sender.hasPermission(perm) || sender.hasPermission("luckybroadcast.admin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("maintenance")) {
            if (!hasPermission(sender, "luckybroadcast.maintenance")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <seconds|cancel>");
                return true;
            }

            if (args[0].equalsIgnoreCase("cancel")) {
                if (maintenanceCountdownTask != null) {
                    maintenanceCountdownTask.cancel();
                    maintenanceCountdownTask = null;
                    sender.sendMessage(ChatColor.GREEN + "Maintenance countdown has been canceled.");
                } else {
                    sender.sendMessage(ChatColor.RED + "No maintenance countdown is currently running.");
                }
                return true;
            }

            if (!args[0].matches("\\d+")) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <seconds|cancel>");
                return true;
            }

            int seconds = Integer.parseInt(args[0]);
            if (seconds > 60) {
                sender.sendMessage(ChatColor.RED + "The maximum time is 60 seconds.");
                return true;
            }

            String title = ChatColor.RED + "NOTICE";
            String subtitlePrefix = "Maintenance will start in ";

            if (maintenanceCountdownTask != null) {
                maintenanceCountdownTask.cancel();
            }

            maintenanceCountdownTask = new BukkitRunnable() {
                int count = seconds;

                @Override
                public void run() {
                    if (count <= 0) {
                        cancel();

                        Bukkit.broadcastMessage(systemPrefix + ChatColor.RED + "Maintenance starting now...");

                        new BukkitRunnable() {
                            int dotCount = 0;

                            @Override
                            public void run() {
                                if (dotCount <= 3) {
                                    StringBuilder dots = new StringBuilder();
                                    for (int i = 0; i < dotCount; i++) {
                                        dots.append(". ");
                                    }
                                    String titleText = ChatColor.RED + "Starting Maintenance" + (dotCount == 0 ? "" : " " + dots.toString());
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
                                        player.sendTitle(titleText, ChatColor.YELLOW + "", 10, 20, 10);
                                    }
                                    dotCount++;
                                } else {
                                    cancel();
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mt on");
                                }
                            }
                        }.runTaskTimer(LuckyBroadcast.this, 0L, 20L);

                        maintenanceCountdownTask = null;
                        return;
                    }

                    String subtitle = subtitlePrefix + count + " seconds!";
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendTitle(title, ChatColor.YELLOW + subtitle, 10, 60, 10);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    }

                    count--;
                }
            };

            maintenanceCountdownTask.runTaskTimer(this, 0L, 20L);
            return true;

        } else if (command.getName().equalsIgnoreCase("restarting")) {
            if (!hasPermission(sender, "luckybroadcast.restarting")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <seconds|cancel>");
                return true;
            }

            if (args[0].equalsIgnoreCase("cancel")) {
                if (restartingCountdownTask != null) {
                    restartingCountdownTask.cancel();
                    restartingCountdownTask = null;
                    if (sender instanceof Player) {
                        ((Player)sender).sendTitle(ChatColor.RED + "Canceled", "", 10, 60, 10);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Restarting countdown has been canceled.");
                } else {
                    sender.sendMessage(ChatColor.RED + "No restarting countdown is currently running.");
                }
                return true;
            }

            if (!args[0].matches("\\d+")) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <seconds|cancel>");
                return true;
            }

            int seconds = Integer.parseInt(args[0]);
            if (seconds > 60) {
                sender.sendMessage(ChatColor.DARK_RED + "The maximum time is 60 seconds.");
                return true;
            }

            String title = ChatColor.RED + "NOTICE";
            String subtitlePrefix = "Restarting will start in ";

            if (restartingCountdownTask != null) {
                restartingCountdownTask.cancel();
            }

            restartingCountdownTask = new BukkitRunnable() {
                int count = seconds;

                @Override
                public void run() {
                    if (count <= 0) {
                        cancel();

                        Bukkit.broadcastMessage(systemPrefix + ChatColor.RED + "Restarting now...");
                        new BukkitRunnable() {
                            int dotCount = 0;

                            @Override
                            public void run() {
                                if (dotCount <= 3) {
                                    StringBuilder dots = new StringBuilder();
                                    for (int i = 0; i < dotCount; i++) {
                                        dots.append(". ");
                                    }
                                    String titleText = ChatColor.RED + "Restarting" + (dotCount == 0 ? "" : " " + dots.toString());
                                    for (Player player : Bukkit.getOnlinePlayers()) 
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
                                        player.sendTitle(titleText, ChatColor.YELLOW + "Server will stop.", 10, 20, 10);
                                    }
                                    dotCount++;
                                } else {
                                    cancel();
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                                }
                            }
                        }.runTaskTimer(LuckyBroadcast.this, 0L, 20L);

                        restartingCountdownTask = null;
                        return;
                    }

                    String subtitle = subtitlePrefix + count + " seconds!";
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendTitle(title, ChatColor.YELLOW + subtitle, 10, 60, 10);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    }

                    count--;
                }
            };

            restartingCountdownTask.runTaskTimer(this, 0L, 20L);
            return true;

        } else if (command.getName().equalsIgnoreCase("bc")) {
            if (!hasPermission(sender, "luckybroadcast.broadcast")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: /bc <message>");
                return true;
            }

            String msg = String.join(" ", args);
            Bukkit.broadcastMessage(broadcastPrefix + ChatColor.WHITE + msg);
            return true;
        }

        return false;
    }
}
