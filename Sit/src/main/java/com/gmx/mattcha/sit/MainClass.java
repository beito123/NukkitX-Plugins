package com.gmx.mattcha.sit;

/*
* Sit
*
* Copyright (c) 2020 beito
*
* This software is released under the GPLv3.
* https://www.gnu.org/licenses/gpl-3.0.en.html
*/

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.gmx.mattcha.sit.entity.Chair;
import com.gmx.mattcha.sit.util.CustomMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainClass extends PluginBase {

    public final int LangFileVersion = 2;

    public CustomMessage msg;

    public boolean enabledTapWarp;
    public boolean enabledBadhackKickFlying;

    private SitAPI sitAPI;

    @Override
    public void onEnable() {
        // Save a lang file from resource
        List<String> langList = new ArrayList<>(Arrays.asList("eng", "jpn"));

        this.msg = new CustomMessage(this, langList, "eng", LangFileVersion);

        // Load config file

        this.saveResource("config.yml");
        Config configFile = new Config(new File(this.getDataFolder(), "config.yml"));
        this.enabledTapWarp = configFile.getBoolean("tapwarp.enabled");
        this.enabledBadhackKickFlying = configFile.getBoolean("badhack.kickFlying");

        // Translate command messages

        Command cmdSit = Server.getInstance().getCommandMap().getCommand("sit");
        if (cmdSit != null) {
            cmdSit.setUsage("/sit");
            cmdSit.setDescription(this.msg.getMessage("command.sit.description"));
        }

        // Register a entity
        Entity.registerEntity("Chair", Chair.class, true);

        // Listen events
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        // Ready API
        this.sitAPI = new SitAPI(this);
    }

    @Override
    public void onDisable() {
        // close all chairs
        SitAPI.getInstance().closeAllChairs();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equals("sit")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            this.msg.send(sender, "command.sit.ingame");
            return true;
        }

        Player player = (Player) sender;

        if (SitAPI.getInstance().hasSat(player)) {
            this.sitAPI.closeChair(player);

            this.msg.sendTip(player, "command.sit.standup");
            return true;
        }

        this.sitAPI.sitEntity(player);

        this.msg.sendTip(player, "command.sit.ok");

        return true;
    }
}
