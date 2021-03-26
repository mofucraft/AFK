package com.yiorno.afk;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.broadcastMessage;
import static org.bukkit.Bukkit.getPlayer;

public class Automation {

    public void checkAFK() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location nowLoc = player.getLocation();
            Location latestLoc = val.map.get(player.getPlayer());


            if (latestLoc == null) {
                val.map.put(player.getPlayer(), nowLoc);
                broadcastMessage("1");
                return;
            }

            if (player.getLocation() == val.map.get(player.getPlayer())) {
                ChangeMode changeMode = new ChangeMode();
                changeMode.ToAFK(player);
            } else {
                val.map.replace(player.getPlayer(), nowLoc);
            }
        }
    }
}
