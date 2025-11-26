package com.yiorno.afk;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class AFK extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        Config config = new Config(this);
        config.load();

        getLogger().info("離席管理が起動しました");
        getServer().getPluginManager().registerEvents(new Event(), this);

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                Automation automation = new Automation();
                automation.checkAFK();
            }
        }, 0L, Config.checkingInterval * 20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("離席管理が停止しました");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("afk")){

            Player p = (Player)sender;
            //User user = (User)sender;

            if(Val.afkplayer.contains(p)) {

                p.sendMessage(ChatColor.YELLOW + "すでに離席中になっています＾～＾");
                return true;

            } else {

                String reason;
                reason = (args.length == 0) ? null : args[0];
                ChangeMode changeMode = new ChangeMode();
                changeMode.ToAFK(p, reason);
                return true;

            }
        }

        return false;
    }
}
