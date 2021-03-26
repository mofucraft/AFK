package com.yiorno.afk;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.broadcastMessage;
import static org.bukkit.Bukkit.getLogger;

public class ChangeMode {
    public void ToAFK(Player player) {
        if(val.afkplayer.contains(player.getPlayer())) {

            val.afkplayer.remove(player.getPlayer());
            player.sendMessage(ChatColor.YELLOW + "すでに離席中になっています＾～＾");

        } else {

            getLogger().info(player.getName() + " が離席しました");
            broadcastMessage(ChatColor.YELLOW + player.getName() + " が離席しました");

            //ImmutableContextSet contextSet = luckPerms.getContextManager().getContext(player);
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            String command = "lp user " + player.getName() + " parent add afk";
            Bukkit.dispatchCommand(console, command);

            val.afkplayer.add(player.getPlayer());

            val.map.put(player.getPlayer(), player.getLocation());
        }
    }

    public void comeBack(Player player) {
        if (val.afkplayer.contains(player.getPlayer())) {
            val.afkplayer.remove(player.getPlayer());
            broadcastMessage(ChatColor.YELLOW + player.getName() + " が帰ってきました");
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            String command = "lp user " + player.getName() + " parent remove afk";
            Bukkit.dispatchCommand(console, command);
        }
    }
}
