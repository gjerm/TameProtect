package eu.crypticcraft.tameprotect;

import eu.crypticcraft.tameprotect.Utils.Pair;
import eu.crypticcraft.tameprotect.Utils.TameProtectConfigHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by dfood on 2016-04-30.
 */
public class TameProtect extends JavaPlugin {
    TameProtectConfigHandler config;
    Set<UUID> tameOut = new HashSet<UUID>();
    HashMap<UUID, Pair<String, String>> commandQueue = new HashMap<UUID, Pair<String, String>>();
    HashMap<UUID, TameProtection> protections = new HashMap<UUID, TameProtection>();

    @Override
    public void onEnable() {
        this.config = new TameProtectConfigHandler(this);
        this.getServer().getPluginManager().registerEvents(new TameProtectListener(this), this);
    }

    public TameProtectConfigHandler getConfigProtections() {
        return this.config;
    }
    public HashMap<UUID, TameProtection> getProtections() {
        return protections;
    }

    public HashMap<UUID, Pair<String, String>> getCommandQueue() {
        return commandQueue;
    }

    public Set<UUID> getTimeOut () {
        return tameOut;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tprot")) {
            if (args.length == 1) {
                queueCommand(this.getServer().getPlayer(sender.getName()).getUniqueId(), args[0], "");
            } else if (args.length >= 2) {
                queueCommand(this.getServer().getPlayer(sender.getName()).getUniqueId(), args[0], args[1]);
            }
        }
        return true;
    }



    private void queueCommand(final UUID player, String intent, String command) {
        commandQueue.put(player, new Pair<String, String>(intent, command));

        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                commandQueue.remove(player);
            }
        }, 200L);
    }
    // Timeout for taming so the correct name is set
    public void tameOut(final UUID player) {
        tameOut.add(player);

        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                tameOut.remove(player);
            }
        }, 20L);
    }

}
