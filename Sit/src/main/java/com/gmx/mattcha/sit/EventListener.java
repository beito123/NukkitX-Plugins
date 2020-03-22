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
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockStairs;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDespawnEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import com.gmx.mattcha.sit.entity.Chair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static cn.nukkit.network.protocol.InteractPacket.ACTION_VEHICLE_EXIT;

public class EventListener implements Listener {

    private MainClass plugin;

    private Map<UUID, Long> tempTap = new HashMap<>();
    private Map<UUID, Long> tempSitBlock = new HashMap<>(); // for bad hack
    private Map<UUID, TempData> tempJustStoodup = new HashMap<>(); // for bad hack

    static class TempData { // for bad hack
        public Long timestamp;
        public Position pos;

        public TempData(Long timestamp, Position pos) {
            this.timestamp = timestamp;
            this.pos = pos;
        }
    }

    public EventListener(MainClass plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) { // for bad hack
        Player player = event.getPlayer();
        if (!this.tempJustStoodup.containsKey(player.getUniqueId())) {
            return;
        }
        if (event.getReasonEnum() != PlayerKickEvent.Reason.FLYING_DISABLED) {
            return;
        }

        TempData data = this.tempJustStoodup.get(player.getUniqueId());

        if (System.currentTimeMillis() - data.timestamp > 1000 * 5) { // 5s
            return;
        }

        if (player.distance(data.pos) < 10D && player.level.getName().equals(data.pos.getLevel().getName())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onTapBlock(PlayerInteractEvent event) {
        if (!this.plugin.enabledTapWarp) {
            return;
        }

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

                this.plugin.msg.sendTip(player, "sit.tapwarp.atStair.ok");
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

            if (!this.plugin.hasSat(player)) {
                return;
            }

            InteractPacket pk = (InteractPacket) event.getPacket();

            if (pk.action == ACTION_VEHICLE_EXIT) {
                if (this.plugin.enabledTapWarp && tempSitBlock.containsKey(player.getUniqueId())) { // bad hack :P
                    long diff = System.currentTimeMillis() - tempSitBlock.get(player.getUniqueId());
                    if (diff < 1000 * 1) {
                        return; //ignore
                    }

                    tempTap.remove(player.getUniqueId());
                }

                this.plugin.closeChair(player);
                this.tempJustStoodup.put(player.getUniqueId(), new TempData(System.currentTimeMillis(), player)); // bad hack
            }
        }
    }
}
