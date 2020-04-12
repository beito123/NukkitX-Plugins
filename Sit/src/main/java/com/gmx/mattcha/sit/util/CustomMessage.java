package com.gmx.mattcha.sit.util;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.gmx.mattcha.util.BaseCustomMessage;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CustomMessage extends BaseCustomMessage {

    public final String KEY_VERSION = "version";
    public final String LANG_FILE = "lang.yml";

    public static CustomMessage FromSOMap(Map<String, Object> msgs) {
        CustomMessage cm = new CustomMessage();
        for (Map.Entry<String, Object> e : msgs.entrySet()) {
            cm.setMessage(e.getKey(), e.getValue().toString());
        }

        return cm;
    }

    public CustomMessage() {

    }

    public CustomMessage(Map<String, String> msgs) {
        this.messages = msgs;
    }


    public CustomMessage(File file) {
        Config langFile = new Config(file);

        for (Map.Entry<String, Object> e : langFile.getAll().entrySet()) {
            this.setMessage(e.getKey(), e.getValue().toString());
        }
    }

    public CustomMessage(PluginBase plugin, List<String> langList, String defLang, int latestVersion) {
        String langName = defLang;
        if (langList.contains(Server.getInstance().getLanguage().getLang())) {
            langName = Server.getInstance().getLanguage().getLang();
        }

        String fileName = "lang-" + langName + ".yml";
        File outFile = new File(plugin.getDataFolder(), LANG_FILE);
        plugin.saveResource(fileName, LANG_FILE, false);

        Config langConfig = new Config(outFile, Config.YAML);
        for (Map.Entry<String, Object> e : langConfig.getAll().entrySet()) {
            this.setMessage(e.getKey(), e.getValue().toString());
        }

        this.fill(plugin, fileName, outFile, latestVersion);
    }

    private void fill(PluginBase plugin, String source, File out, int latestVersion) {
        Config langConfig = new Config(out, Config.YAML);

        boolean needFill = false;

        if (!langConfig.exists(KEY_VERSION)) {
            langConfig.set(KEY_VERSION, 0);

            needFill = true;
        }

        int ver = langConfig.getInt(KEY_VERSION);

        if (ver >= latestVersion && !needFill) {
            return;
        }

        File tempFile = new File(out.getParentFile(), "temp.yml");
        plugin.saveResource(source, "temp.yml", true);

        Config tempConfig = new Config(tempFile, Config.YAML);
        langConfig.setDefault(tempConfig.getRootSection());
        langConfig.set(KEY_VERSION, tempConfig.get(KEY_VERSION));

        langConfig.save();

        tempFile.delete();
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
