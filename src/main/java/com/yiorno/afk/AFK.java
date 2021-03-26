package com.yiorno.afk;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.broadcastMessage;

public final class AFK extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getLogger().info("離席管理が起動しました");
        getServer().getPluginManager().registerEvents(this, this);

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                Automation automation = new Automation();
                automation.checkAFK();
            }
        }, 0L, 5*20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("離席管理が停止しました");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("afk")){
            Player player = (Player)sender;
            //User user = (User)sender;

            ChangeMode changeMode = new ChangeMode();
            changeMode.ToAFK(player);
        }
        return false;
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (e.getTo().getBlockX() == e.getFrom().getBlockX()
                && e.getTo().getBlockY() == e.getFrom().getBlockY()
                && e.getTo().getBlockZ() == e.getFrom().getBlockZ()) {
            return;
        }

        ChangeMode changeMode = new ChangeMode();
        changeMode.comeBack(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (val.afkplayer.contains(player.getPlayer())) {
            val.afkplayer.remove(player.getPlayer());
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            String command = "/ user " + player.getName() + " parent remove afk";
            Bukkit.dispatchCommand(console, command);
        }
    }

}
