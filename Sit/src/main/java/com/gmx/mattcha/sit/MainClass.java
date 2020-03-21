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
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainClass extends PluginBase {

    private Map<UUID, Chair> usingChairs = new HashMap<>();

    @Override
    public void onEnable() {
        Entity.registerEntity("Chair", Chair.class, true);

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
            sender.sendMessage(TextFormat.RED + "Please run the command in game");
            return true;
        }

        Player player = (Player) sender;

        this.sitPlayer(player, player.add(0, 1.15, 0), new Vector3f(0, 0, 0));
        player.sendTip("Sit here! Jump to stand up.");

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
