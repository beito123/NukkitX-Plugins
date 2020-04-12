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
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.gmx.mattcha.sit.entity.Chair;
import com.gmx.mattcha.sit.event.PlayerSitEvent;
import com.gmx.mattcha.sit.util.CustomMessage;

import java.io.File;
import java.util.*;

public class MainClass extends PluginBase {

    public final int LangFileVersion = 2;

    public CustomMessage msg;

    private Map<UUID, Chair> usingChairs = new HashMap<>();

    public boolean enabledTapWarp;
    public boolean enabledBadhackKickFlying;

    public Vector3 defaultSitPosition = new Vector3(0, 0F, 0);
    public Vector3f defaultSitOffset = new Vector3f(0, 1.05F, 0);

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
    }

    @Override
    public void onDisable() {
        for (Map.Entry<UUID, Chair> entry : usingChairs.entrySet()) {
            entry.getValue().close();
        }
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

        if (this.hasSat(player)) {
            this.closeChair(player);

            this.msg.sendTip(player, "command.sit.standup");
            return true;
        }

        this.sitPlayer(player, player.add(defaultSitPosition), defaultSitOffset);// adjust

        this.msg.sendTip(player, "command.sit.ok");
        Server.getInstance().getLogger().info(TextFormat.BLUE + player.asVector3f().toString());

        return true;
    }

    // API

    public boolean hasSat(Player player) {
        return this.usingChairs.containsKey(player.getUniqueId());
    }

    public void closeChair(Player player) {
        Chair chair = this.getChair(player);
        if (chair == null) {
            return;
        }

        this.usingChairs.remove(player.getUniqueId());

        if (chair.isClosed()) {
            return;
        }

        for (Entity passenger : new ArrayList<>(chair.getPassengers())) {
            if (passenger == null) {
                continue;
            }

            chair.dismountEntity(passenger);
        }

        chair.close();
    }

    public Chair getChair(Player player) {
        if (!this.hasSat(player)) {
            return null;
        }

        return this.usingChairs.get(player.getUniqueId());
    }

    public boolean sitPlayer(Player player, Vector3 pos, Vector3f offset) {
        closeChair(player);

        PlayerSitEvent ev;
        Server.getInstance().getPluginManager().callEvent(ev = new PlayerSitEvent(this, player));
        if (ev.isCancelled()) {
            return false;
        }
        CompoundTag nbt = Entity.getDefaultNBT(pos, new Vector3(), (float) player.getYaw(), 0);

        Chair chair = (Chair) Entity.createEntity("Chair", player.chunk, nbt);

        chair.MountedOffset = offset;
        chair.setSeatPosition(new Vector3f(0, 0, 0)); // is not applied...

        chair.spawnToAll();
        chair.mountEntity(player);

        this.usingChairs.put(player.getUniqueId(), chair);

        return true;
    }

    public void standupPlayer(Player player) {
        closeChair(player);
    }
}
