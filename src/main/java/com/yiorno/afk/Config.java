package com.yiorno.afk;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

//Quorted from HimaJyn
public class Config {

    private final Plugin plugin;
    private FileConfiguration config = null;

    public static Integer checkingInterval;
    public static Integer setAFKInterval;

    public Config(Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        if (config != null) {
            plugin.reloadConfig();
        }
        config = plugin.getConfig();

        checkingInterval = config.getInt("checking-interval");
        setAFKInterval = config.getInt("set-afk-interval");

    }

}