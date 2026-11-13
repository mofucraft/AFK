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
}
