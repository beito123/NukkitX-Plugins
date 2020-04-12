package com.gmx.mattcha.sit;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import com.gmx.mattcha.sit.entity.Chair;
import com.gmx.mattcha.sit.event.PlayerSitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SitAPI {

    private static SitAPI INSTANCE;

    private MainClass plugin;

    protected SitAPI(MainClass plugin) {
        this.plugin = plugin;

        INSTANCE = this;
    }

    public static SitAPI getInstance() {
        return INSTANCE;
    }

    public Vector3 defaultSitPosition = new Vector3(0, 0F, 0);
    public Vector3f defaultSitOffset = new Vector3f(0, 1.05F, 0);

    public Vector3 defaultSitStairPosition = new Vector3(0.5, 0, 0.5);
    public Vector3f defaultSitStairOffset = new Vector3f(0 , 1.58F, 0);

    private Map<Long, Chair> usedChairs = new HashMap<>();

    public boolean hasSat(Entity entity) {
        return this.usedChairs.containsKey(entity.getId());
    }

    public Chair getChair(Entity entity) {
        if (!this.hasSat(entity)) {
            return null;
        }

        return this.usedChairs.get(entity.getId());
    }

    public void closeChair(Entity entity) {
        Chair chair = this.getChair(entity);
        if (chair == null) {
            return;
        }

        this.usedChairs.remove(entity.getId());

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

    public void closeAllChairs() {
        for (Map.Entry<Long, Chair> e : this.usedChairs.entrySet()) {
            e.getValue().close();
        }
    }

    public boolean sitEntity(Entity entity) {
        return this.sitEntity(entity, entity.add(defaultSitPosition));
    }

    public boolean sitEntity(Entity entity, Vector3 pos) {
        return this.sitEntity(entity, pos, defaultSitOffset);
    }

    public boolean sitEntity(Entity entity, Vector3 pos, Vector3f offset) {
        closeChair(entity);

        if (entity instanceof Player) {
            Player player = (Player) entity;

            PlayerSitEvent ev;
            Server.getInstance().getPluginManager().callEvent(ev = new PlayerSitEvent(this.plugin, player));
            if (ev.isCancelled()) {
                return false;
            }
        }

        CompoundTag nbt = Entity.getDefaultNBT(pos, new Vector3(), (float) entity.getYaw(), 0);

        Chair chair = (Chair) Entity.createEntity("Chair", entity.chunk, nbt);

        chair.MountedOffset = offset;
        chair.setSeatPosition(new Vector3f(0, 0, 0)); // is not applied...

        chair.spawnToAll();
        chair.mountEntity(entity);

        this.usedChairs.put(entity.getId(), chair);

        return true;
    }

    public void standupEntity(Entity entity) {
        closeChair(entity);
    }
}
