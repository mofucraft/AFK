package com.yiorno.afk;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Val {
    public static Map<Player, Integer> map = new HashMap<>();

    public static List<Player> afkplayer = new ArrayList<>();

    // 視点の向きの変化の合計（度）。checking-interval ごとに空にする
    public static Map<Player, Float> lookSum = new HashMap<>();

    // v1.6: 最後に読んだ視点の向き {yaw, pitch}（PlayerMoveEvent が来ない乗り物・椅子でも毎秒読んで比べる）
    public static Map<Player, float[]> lastRot = new HashMap<>();

    // v1.6: 最後にキーか視点が動いた時刻（ms）。離席中のクリックを空振りにする前に見る
    public static Map<Player, Long> lastTouch = new HashMap<>();
}
