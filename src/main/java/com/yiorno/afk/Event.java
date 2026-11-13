package com.yiorno.afk;

import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Event implements Listener {

    // v1.6: 「操作した」と数えるのは人の指が触った物だけ = 移動キーの状態の変化・視点の向きの変化・チャット・コマンド。
    // 位置の変化（PlayerMoveEvent のブロック移動）は数えない（水流・水流エレベーター・ピストン・乗り物で運ばれても離席になる）。
    // クリック・攻撃・GUI のクリックは数えない（自動連打中は放置と判定する）
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInput(PlayerInputEvent e) {
        Input now = e.getInput();
        Input before = e.getPlayer().getCurrentInput();
        if (now.isForward() == before.isForward()
                && now.isBackward() == before.isBackward()
                && now.isLeft() == before.isLeft()
                && now.isRight() == before.isRight()
                && now.isJump() == before.isJump()
                && now.isSneak() == before.isSneak()
                && now.isSprint() == before.isSprint()) {
            return;
        }
        Val.lastTouch.put(e.getPlayer(), System.currentTimeMillis());
        Val.lookSum.remove(e.getPlayer());
        markActive(e.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) {
            return;
        }

        rotated(p, to.getYaw(), to.getPitch());
    }

    // テレポート（Skript・/spawn・ワールドの移動）で向きが変わっても「操作した」と数えない
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Location to = e.getTo();
        if (to != null) {
            Val.lastRot.put(e.getPlayer(), new float[]{to.getYaw(), to.getPitch()});
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        Location to = e.getRespawnLocation();
        Val.lastRot.put(e.getPlayer(), new float[]{to.getYaw(), to.getPitch()});
    }

    // 椅子・乗り物の間も効くよう、毎秒ここで今の向きを読む（AFK.onEnable の繰り返し）
    static void checkRotation(Player p) {
        Location l = p.getLocation();
        rotated(p, l.getYaw(), l.getPitch());
    }

    // 前回読んだ向きと比べる。離席中は少しでも動けば（0 より大きければ）その場で戻す。
    // 離席でない時は小さな揺れで数えないよう、確認の間隔（checking-interval）の中の合計が閾値を超えた時だけ数える
    private static boolean rotated(Player p, float yaw, float pitch) {
        float[] last = Val.lastRot.put(p, new float[]{yaw, pitch});
        if (last == null) {
            return false;
        }
        float d = Math.abs(wrap(yaw - last[0])) + Math.abs(pitch - last[1]);
        if (d == 0f) {
            return false;
        }
        Val.lastTouch.put(p, System.currentTimeMillis());
        if (Val.afkplayer.contains(p)) {
            Val.lookSum.remove(p);
            markActive(p);
            return true;
        }
        float sum = Val.lookSum.getOrDefault(p, 0f) + d;
        if (sum < Config.lookThreshold) {
            Val.lookSum.put(p, sum);
            return false;
        }
        Val.lookSum.remove(p);
        markActive(p);
        return true;
    }

    // 離席中のクリックを空振りにしてよいか。マウスを動かしながら押した一発目を落とさないよう、
    // 今の向きが前回と違う（= まだ読んでいない視点の変化がある）か、直前 1 秒以内にキーか視点が動いていれば空振りにしない
    private static boolean shouldCancel(Player p) {
        if (!Config.cancelActions || p == null || !Val.afkplayer.contains(p)) {
            return false;
        }
        if (rotated(p, p.getLocation().getYaw(), p.getLocation().getPitch())) {
            return false;
        }
        Long t = Val.lastTouch.get(p);
        if (t != null && System.currentTimeMillis() - t <= 1000L) {
            return false;
        }
        return Val.afkplayer.contains(p);
    }

    // v1.6: 離席中は使う・置く・壊す・攻撃するを黙って空振りにする（メッセージ・音・ログは出さない）。
    // 戻るのはキー・視点・チャット・コマンドだけなので、空振りしたクリックで離席が解けることはない。config の afk-cancel-actions: false で切れる
    private static void cancelIfAfk(Player p, Cancellable e) {
        if (shouldCancel(p)) {
            e.setCancelled(true);
        }
    }

    private static Player attacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player) {
            return (Player) ((Projectile) damager).getShooter();
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        if (shouldCancel(e.getPlayer())) {
            e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            e.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractEntity(PlayerInteractEntityEvent e) { cancelIfAfk(e.getPlayer(), e); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) { cancelIfAfk(e.getPlayer(), e); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e) { cancelIfAfk(e.getPlayer(), e); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageBlock(BlockDamageEvent e) { cancelIfAfk(e.getPlayer(), e); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttack(EntityDamageByEntityEvent e) { cancelIfAfk(attacker(e.getDamager()), e); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHangingBreak(HangingBreakByEntityEvent e) { cancelIfAfk(attacker(e.getRemover()), e); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) { cancelIfAfk(e.getPlayer(), e); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketFill(PlayerBucketFillEvent e) { cancelIfAfk(e.getPlayer(), e); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFish(PlayerFishEvent e) { cancelIfAfk(e.getPlayer(), e); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onConsume(PlayerItemConsumeEvent e) { cancelIfAfk(e.getPlayer(), e); }

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
