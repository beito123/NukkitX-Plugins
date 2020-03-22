package com.gmx.mattcha.sit.util;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.gmx.mattcha.util.BaseCustomMessage;

import java.util.Map;

public class CustomMessage extends BaseCustomMessage {
    public static CustomMessage FromSOMap(Map<String, Object> msgs) {
        CustomMessage cm = new CustomMessage();
        for (Map.Entry<String, Object> e : msgs.entrySet()) {
            cm.setMessage(e.getKey(), e.getValue().toString());
        }

        return cm;
    }

    public CustomMessage(Map<String, String> msgs) {
        this.messages = msgs;
    }

    public CustomMessage() {
    }

    @Override
    public String getMessage(String key, String ...args) {
        return TextFormat.RESET + super.getMessage(key, args) + TextFormat.RESET;
    }

    public void send(CommandSender to, String key, String ...args) {
        to.sendMessage(this.getMessage(key, args));
    }

    public void sendTip(Player to, String key, String ...args) {
        to.sendTip(this.getMessage(key, args));
    }

    public void sendTitle(Player to, String key, String ...args) {
        to.sendTitle(this.getMessage(key, args));
    }

    public void sendActionBar(Player to, String key, String ...args) {
        to.sendActionBar(this.getMessage(key, args));
    }

    public void sendPopup(Player to, String key, String ...args) {
        to.sendPopup(this.getMessage(key, args));
    }
}
