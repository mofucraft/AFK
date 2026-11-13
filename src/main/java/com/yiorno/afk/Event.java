package com.yiorno.afk;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Event implements Listener {

    // v1.4: 活動とみなすのは「ブロックをまたぐ移動」と「視点の向きの変化」（v1.5 でチャットとコマンドを足した）
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
        markActive(p);
    }

    // v1.5: チャットとコマンドも「操作した」と数える（GUI のクリックは数えないまま）
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        // 非同期で来るので、判定はメインスレッドへ戻してから
        Bukkit.getScheduler().runTask(AFK.getInstance(), () -> {
            if (p.isOnline()) {
                markActive(p);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String label = e.getMessage().split(" ", 2)[0].toLowerCase();
        // /afk 自体は数えない（離席中に /afk を打つと「帰ってきました」→「離席しました」が続けて出るため）
        if (label.equals("/afk") || label.endsWith(":afk")) {
            return;
        }
        markActive(e.getPlayer());
    }

    private static void markActive(Player p) {
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
