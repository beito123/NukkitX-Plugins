package com.gmx.mattcha.roto;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDespawnEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.BlockFace;
import cn.nukkit.utils.Faceable;
import com.gmx.mattcha.sit.SitAPI;

public class EventListener implements Listener {

    private MainClass plugin;

    public EventListener(MainClass plugin) {
        this.plugin = plugin;
    }

    public MainClass getPlugin() {
        return this.plugin;
    }

    @EventHandler
    public void onEntityTap(PlayerInteractEntityEvent event) {
        Entity entity = event.getEntity();

        /*if (!this.plugin.npcList.containsKey(entity.getId())) {
            return;
        }*/

        Item item = event.getItem();
        if (item.getId() != ItemID.POTATO) {
            return;
        }

        if (SitAPI.getInstance().hasSat(entity)) {
            SitAPI.getInstance().standupEntity(entity);
            return;
        }

        SitAPI.getInstance().sitEntity(entity);
    }

    @EventHandler
    public void onDespawn(EntityDespawnEvent event) {
        Entity entity = event.getEntity();

        if (!this.plugin.npcList.containsKey(entity.getId())) {
            return;
        }

        this.plugin.npcList.remove(entity.getId());
    }

    @EventHandler
    public void onTap(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.isBlockMode(player)) {
            return;
        }

        Block block = event.getBlock();

        if (block.getId() == BlockID.AIR) {
            return;
        }

        String msg = "---------- Block Info(" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ") ----------\n";

        msg += "Block: " + block.getName() + "(ID=" + block.getId() + ", Damage=" + block.getDamage() + ", SaveID=" + block.getSaveId() + ")\n";
        msg += "Chunk X=" + block.getChunkX() + ", Y=" + block.getChunkZ() + "\n";

        if (block instanceof Faceable) {
            BlockFace face = ((Faceable) block).getBlockFace();
            msg += "BlockFace: " + face.getName() + "(Index=" + face.getIndex() + ", Axis=" + face.getAxis() + ", HIndex=" + face.getHorizontalIndex() + ", HAngle=" + face.getHorizontalAngle() + ")\n";
        }

        player.sendMessage(msg);
    }
}
