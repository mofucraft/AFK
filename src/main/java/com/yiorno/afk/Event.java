package com.yiorno.afk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Event implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (e.getTo().getBlockX() == e.getFrom().getBlockX()
                && e.getTo().getBlockY() == e.getFrom().getBlockY()
                && e.getTo().getBlockZ() == e.getFrom().getBlockZ()) {
            return;
        }

        ChangeMode changeMode = new ChangeMode();
        changeMode.comeBack(p);
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

    @EventHandler
    public void onInteract(PlayerInteractEvent e){

        ChangeMode cm = new ChangeMode();

        if(!(Val.afkplayer.contains(e.getPlayer()))){
            cm.active(e.getPlayer());
            return;
        }

        cm.comeBack(e.getPlayer());

    }

    @EventHandler
    public void onPunch(EntityDamageByEntityEvent e){

        if(!(e.getDamager() instanceof Player)){
            return;
        }

        Player p = (Player) e.getDamager();
        ChangeMode cm = new ChangeMode();

        if(Val.afkplayer.contains(p)){
            cm.comeBack(p);
            return;
        }

        cm.active(p);

    }

}
