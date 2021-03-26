package com.yiorno.afk;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class Automation {

    public void checkAFK() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            Integer count = val.map.get(player.getPlayer());

            if (count == null) {
                val.map.put(player.getPlayer(), 1);
                return;
            }

            //30sごとにチェックなので2倍にする
            if (count >= 5*2) {
                ChangeMode changeMode = new ChangeMode();
                changeMode.ToAFK(player);
            } else {
                count++;
                val.map.replace(player.getPlayer(), count);
            }
        }
    }
}
