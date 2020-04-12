package com.gmx.mattcha.roto;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import com.gmx.mattcha.roto.entity.EntityNPC;

import java.util.*;

public class MainClass extends PluginBase {

    private List<UUID> tempBlockMode = new ArrayList<>();
    public Map<Long, EntityNPC> npcList = new HashMap<>();

    @Override
    public void onEnable() {
        // Register a entity
        Entity.registerEntity("EntityNPC", EntityNPC.class, true);

        // Register event listener
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        Server.getInstance().getLogger().notice(TextFormat.YELLOW + "Enabled Roto! " +
                TextFormat.RED + "Don't use in production server!");
    }

    // Commands

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "roto":
                if (args.length < 1) {
                    return false;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(TextFormat.RED + "Please run the command in game");
                    return true;
                }

                Player player = (Player) sender;

                switch (args[0]) {
                    case "block":
                    case "b":
                        if (this.isBlockMode(player)) {
                            this.removeBlockMode(player);
                            sender.sendMessage("You are not block mode");
                            return true;
                        }

                        this.addBlockMode(player);
                        sender.sendMessage("You are block mode");
                        break;
                    default:
                        sender.sendMessage(
                            "----- Roto Commands -----\n" +
                            "/roto <help> - This is not used now :P\n" +
                            "/npc <make(m)|remove(r)|clone(c)> - Make a fake NPC\n"
                        );
                }

                return true;
            case "npc":
                return this.execNPCCommand(sender, args);
        }

        return false;
    }

    private boolean execNPCCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "Please run the command in game");
            return true;
        }

        Player player = (Player) sender;

        switch (args[0]) {
            case "clone":
            case "c":
                this.clonePlayerNPC(player);
                return true;
            default:
                sender.sendMessage(
                    "----- Roto Commands -----\n" +
                    "/roto <help> - This is not used now :P\n" +
                    "/npc <make(m)|remove(r)|clone(c)> - Make a fake NPC\n"
                );
                return true;
        }
    }

    // API - BlockMode

    public boolean isBlockMode(Player player) {
        return this.tempBlockMode.contains(player.getUniqueId());
    }

    public boolean addBlockMode(Player player) {
        return this.tempBlockMode.add(player.getUniqueId());
    }

    public boolean removeBlockMode(Player player) {
        return this.tempBlockMode.remove(player.getUniqueId());
    }

    // API - NPC

    public void clonePlayerNPC(Player player) {
        CompoundTag nbt = Entity.getDefaultNBT(player, new Vector3(), (float) player.getYaw(), (float) player.getPitch());

        EntityNPC enpc = (EntityNPC) Entity.createEntity("EntityNPC", player.chunk, nbt, player.getSkin());

        enpc.spawnToAll();

        this.npcList.put(enpc.getId(), enpc);
    }
}
