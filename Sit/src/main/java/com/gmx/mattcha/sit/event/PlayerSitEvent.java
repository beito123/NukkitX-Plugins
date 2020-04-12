package com.gmx.mattcha.sit.event;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import com.gmx.mattcha.sit.MainClass;

public class PlayerSitEvent extends SitPluginEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Player player;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public PlayerSitEvent(MainClass plugin, Player player) {
        super(plugin);

        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
