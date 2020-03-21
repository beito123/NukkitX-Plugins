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
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockStairs;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDespawnEvent;
import cn.nukkit.event.player.PlayerBedEnterEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static cn.nukkit.network.protocol.InteractPacket.ACTION_VEHICLE_EXIT;

public class EventListener implements Listener {

    private MainClass plugin;

    private Map<UUID, Long> tempTap = new HashMap<>();
    private Map<UUID, Long> tempSitBlock = new HashMap<>();

    public EventListener(MainClass plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTap(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.hasSat(player)) {
            return;
        }

        if (!this.tempTap.containsKey(player.getUniqueId())) { // First tap
            tempTap.put(player.getUniqueId(), System.currentTimeMillis());
        }

        // Second tap

        long diff = System.currentTimeMillis() - this.tempTap.get(player.getUniqueId());
        if (diff <= 1000 * 0.8) { // 0.8s
            Block block = event.getBlock();
            if (block instanceof BlockStairs && (block.getDamage() & 0x04) == 0) {
                tempSitBlock.put(player.getUniqueId(), System.currentTimeMillis()); // bad hack :P

                this.plugin.sitPlayer(player, block.add(0.5, 1.6, 0.5), new Vector3f(0 , 0, 0));

                player.sendTip(TextFormat.GOLD + "Jump to stand up" + TextFormat.RESET);
            }
        }

        this.tempTap.remove(player.getUniqueId());
    }

    @EventHandler
    public void onDespawn(EntityDespawnEvent event) {
        if (event.getEntity() instanceof Player) {
            this.plugin.closeChair((Player) event.getEntity());
        }
    }

    @EventHandler
    public void onDead(PlayerDeathEvent event) {
        this.plugin.closeChair(event.getEntity());
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        this.plugin.closeChair(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        this.plugin.closeChair(event.getPlayer());
    }

    @EventHandler
    public void onActionPacket(DataPacketReceiveEvent event) {
        if (event.getPacket().pid() == ProtocolInfo.INTERACT_PACKET) {
            Player player = event.getPlayer();
            tempSitBlock.put(player.getUniqueId(), System.currentTimeMillis());

            if (player.getRiding() == null || !(player.getRiding() instanceof Chair)) {
                return;
            }

            InteractPacket pk = (InteractPacket) event.getPacket();

            if (pk.action == ACTION_VEHICLE_EXIT) {
                if (tempSitBlock.containsKey(player.getUniqueId())) { // bad hack :P
                    long diff = System.currentTimeMillis() - tempSitBlock.get(player.getUniqueId());
                    if (diff < 1000 * 2) {
                        return; //ignore
                    }

                    tempTap.remove(player.getUniqueId());
                }

                this.plugin.closeChair(player);
            }
        }
    }
}
