package com.yiorno.afk;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Event implements Listener {

    // v1.4: 活動とみなすのは「ブロックをまたぐ移動」と「視点の向きの変化」だけ
    // クリック（PlayerInteractEvent）と攻撃（EntityDamageByEntityEvent）は数えない（自動連打中は放置と判定する）
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) {
            return;
        }

        boolean moved = to.getBlockX() != from.getBlockX()
                || to.getBlockY() != from.getBlockY()
                || to.getBlockZ() != from.getBlockZ();

        boolean looked = false;
        if (!moved) {
            float dYaw = Math.abs(wrap(to.getYaw() - from.getYaw()));
            float dPitch = Math.abs(to.getPitch() - from.getPitch());
            if (dYaw + dPitch == 0f) {
                return;
            }
            // 小さな揺れで戻らないよう、確認の間隔（checking-interval）の中で合計が閾値を超えた時だけ数える
            float sum = Val.lookSum.getOrDefault(p, 0f) + dYaw + dPitch;
            if (sum < Config.lookThreshold) {
                Val.lookSum.put(p, sum);
                return;
            }
            looked = true;
        }

        if (!moved && !looked) {
            return;
        }

        Val.lookSum.remove(p);
        ChangeMode changeMode = new ChangeMode();

        if (Val.afkplayer.contains(p)) {
            changeMode.comeBack(p);
        } else {
            changeMode.active(p);
        }
    }

    private static float wrap(float deg) {
        deg = deg % 360f;
        if (deg > 180f) deg -= 360f;
        if (deg < -180f) deg += 360f;
        return deg;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        ChangeMode cm = new ChangeMode();
        cm.release(e.getPlayer());

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        ChangeMode cm = new ChangeMode();
        cm.release(e.getPlayer());

    }

}
