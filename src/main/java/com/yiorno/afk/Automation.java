package com.yiorno.afk;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class Automation {

    public void checkAFK() {

        Integer afkCount;
        afkCount = Config.setAFKInterval * 60 / Config.checkingInterval;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Integer count = val.map.get(player.getPlayer());

            if (count == null) {
                val.map.put(player.getPlayer(), 1);
                return;
            }

            if (count >= afkCount) {
                ChangeMode changeMode = new ChangeMode();
                changeMode.ToAFK(player, null);
            } else {
                count++;
                val.map.replace(player.getPlayer(), count);
            }
        }
    }
}
