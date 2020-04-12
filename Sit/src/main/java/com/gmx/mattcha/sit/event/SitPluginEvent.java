package com.gmx.mattcha.sit.event;

import cn.nukkit.event.Event;
import com.gmx.mattcha.sit.MainClass;

public class SitPluginEvent extends Event {

    private MainClass plugin;

    public MainClass getPlugin() {
        return this.plugin;
    }

    public SitPluginEvent(MainClass plugin) {
        this.plugin = plugin;
    }
}
