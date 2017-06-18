package eu.crypticcraft.tameprotect;

import eu.crypticcraft.tameprotect.Classes.MessageInfo;
import eu.crypticcraft.tameprotect.Handlers.CommandHandler;
import eu.crypticcraft.tameprotect.Handlers.DatabaseHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;


public class TameProtect extends JavaPlugin {
    private DatabaseHandler protectionDatabase;
    private CommandHandler commandHandler;
    private static HashMap<UUID, Protection> protections = new HashMap<UUID, Protection>();

    public void loadConfiguration() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    public void reloadConfiguration() {
        this.reloadConfig();
        commandHandler.reload();
    }

    @Override
    public void onEnable() {
        loadConfiguration();
        this.protectionDatabase = new DatabaseHandler(this);
        this.commandHandler = new CommandHandler(this);
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {
        protectionDatabase.saveProtections();
    }

    public DatabaseHandler getProtectionDatabase() {
        return this.protectionDatabase;
    }

    public HashMap<UUID, Protection> getProtections() {
        return protections;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void sendMessage(Player player, String msgType, MessageInfo msgInfo) {
        String message = "";
        if (msgType.equals("info")) {
            message += this.getConfig().getString("messages.info1") + "\n";
            message += this.getConfig().getString("messages.info2") + "\n";
            message += this.getConfig().getString("messages.info3");
        }
        else {
            if (this.getConfig().contains("messages." + msgType)) {
                message = this.getConfig().getString("message_prefix");
                message += this.getConfig().getString("messages." + msgType);
            }
        }
        message = message.replaceAll("&p", msgInfo.playerName);
        message = message.replaceAll("&w", msgInfo.animalName);
        message = message.replaceAll("&t", msgInfo.members);
        if (message.length() > 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
