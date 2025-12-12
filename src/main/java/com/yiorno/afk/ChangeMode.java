package com.yiorno.afk;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

import static org.bukkit.Bukkit.broadcastMessage;
import static org.bukkit.Bukkit.getLogger;

public class ChangeMode {
    public void ToAFK(Player p, String reason) {

        if(!Val.afkplayer.contains(p)) {

            String msg = p.getName() + " が離席しました";
            msg = (reason == null) ? msg : msg + " | " + reason;

            getLogger().info(msg);

            // SuperVanishでInvisible状態の場合は通知を送信しない
            if(!isPlayerInvisible(p)) {
                broadcastMessage(ChatColor.YELLOW + msg);
            }

            Val.afkplayer.add(p);
            lpAdd(p);

        }

    }

    public void comeBack(Player p) {

        if(Val.afkplayer.contains(p)) {
            Val.afkplayer.remove(p);
            Val.map.remove(p);
            Val.map.put(p, 1);

            // SuperVanishでInvisible状態の場合は通知を送信しない
            if(!isPlayerInvisible(p)) {
                broadcastMessage(ChatColor.YELLOW + p.getName() + " が帰ってきました");
            }

            lpRemove(p);
        }

    }

    public void release(Player p){

        Val.afkplayer.remove(p);
        Val.map.remove(p);

        // サーバー移動時に権限が残らないよう、常にlpRemoveを呼び出す
        lpRemove(p);

    }

    public void active(Player p){

        if(Val.afkplayer.contains(p)){
            return;
        }

        Val.map.remove(p);
        Val.map.put(p, 1);

    }

    public void lpAdd(Player p){

        //ImmutableContextSet contextSet = luckPerms.getContextManager().getContext(player);
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String command = "lp user " + p.getName() + " parent add afk";
        Bukkit.dispatchCommand(console, command);

    }

    public void lpRemove(Player p){

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String command = "lp user " + p.getName() + " parent remove afk";
        Bukkit.dispatchCommand(console, command);

    }

    /**
     * SuperVanish/PremiumVanishでプレイヤーがInvisible状態かどうかをチェック
     * プラグインが利用できない場合はfalseを返す
     */
    private boolean isPlayerInvisible(Player p) {
        try {
            // リフレクションを使用してVanishAPIにアクセス
            Class<?> vanishAPI = Class.forName("de.myzelyam.api.vanish.VanishAPI");
            Method isInvisibleMethod = vanishAPI.getMethod("isInvisible", Player.class);
            return (Boolean) isInvisibleMethod.invoke(null, p);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // SuperVanish/PremiumVanishがインストールされていない場合
            return false;
        } catch (Exception e) {
            // その他のエラー
            return false;
        }
    }
}
