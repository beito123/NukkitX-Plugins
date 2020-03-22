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
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.gmx.mattcha.sit.entity.Chair;
import com.gmx.mattcha.sit.util.CustomMessage;

import java.io.File;
import java.util.*;

public class MainClass extends PluginBase {

    public CustomMessage msg;

    private Map<UUID, Chair> usingChairs = new HashMap<>();

    public boolean enabledTapWarp;
    public boolean enabledBadhackKickFlying;

    @Override
    public void onEnable() {
        // Save a lang file from resource
        List<String> langList = new ArrayList<>(Arrays.asList("eng", "jpn"));

        String langName = "eng";
        if (langList.contains(this.getServer().getLanguage().getLang())) {
            langName = this.getServer().getLanguage().getLang();
        }

        this.saveResource("lang-" + langName + ".yml", "lang.yml", false);

        // Load language file

        Config langFile = new Config(new File(this.getDataFolder(), "lang.yml"));

        this.msg = CustomMessage.FromSOMap(langFile.getAll());

        // Load config file

        this.saveResource("config.yml");
        Config configFile = new Config(new File(this.getDataFolder(), "config.yml"));
        this.enabledTapWarp = configFile.getBoolean("tapwarp.enabled");
        this.enabledBadhackKickFlying = configFile.getBoolean("badhack.kickFlying");

        // Translate command messages

        Command cmdSit = Server.getInstance().getCommandMap().getCommand("sit");
        if (cmdSit != null) {
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

        this.sitPlayer(player,
                player.add(0, 1.11, 0), // adjust
                new Vector3f(0, 0, 0));

        this.msg.sendTip(player, "command.sit.ok");

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

        chair.dismountEntity(player);

        chair.close();

        this.usingChairs.remove(player.getUniqueId());
    }

    public Chair getChair(Player player) {
        if (!this.hasSat(player)) {
            return null;
        }

        return this.usingChairs.get(player.getUniqueId());
    }

    public void sitPlayer(Player player, Vector3 pos, Vector3f offset) {
        closeChair(player);

        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<DoubleTag>("Pos")
                        .add(new DoubleTag("", pos.getX()))
                        .add(new DoubleTag("", pos.getY()))
                        .add(new DoubleTag("", pos.getZ())))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", 0))
                        .add(new FloatTag("", 0)));

        Chair chair = (Chair) Entity.createEntity("Chair", player.chunk, nbt);

        chair.spawnToAll();

        chair.setSeatPosition(offset);

        chair.mountEntity(player);

        this.usingChairs.put(player.getUniqueId(), chair);
    }

    public void standupPlayer(Player player) {
        closeChair(player);
    }
}
