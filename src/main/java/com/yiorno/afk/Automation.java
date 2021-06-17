package com.yiorno.afk;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Automation {

    public void checkAFK() {

        int afkCount;
        afkCount = Config.setAFKInterval * 60 / Config.checkingInterval;

        for (Player p : Bukkit.getOnlinePlayers()) {

            Integer count = Val.map.get(p);

            if (count == null) {
                Val.map.put(p, 1);
                return;
            }

            if (count >= afkCount) {

                ChangeMode changeMode = new ChangeMode();
                changeMode.ToAFK(p, null);

            } else {

                count++;
                Val.map.replace(p, count);

            }

        }
    }
}
