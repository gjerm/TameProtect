package eu.crypticcraft.tameprotect;

import eu.crypticcraft.tameprotect.Handlers.CommandHandler;
import eu.crypticcraft.tameprotect.Handlers.DatabaseHandler;
import org.bukkit.ChatColor;
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
        protectionDatabase.reloadProtections();
    }

    @Override
    public void onEnable() {
        loadConfiguration();
        this.protectionDatabase = new DatabaseHandler(this);
        this.commandHandler = new CommandHandler(this);
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
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

    public String getMessage(String msg, String playerName, String animalName) {
        playerName = playerName == null ? "" : playerName;
        animalName = animalName == null ? "" : animalName;
        String m = this.getConfig().getString("message_prefix");
        m += this.getConfig().getString("messages." + msg);
        m = m.replaceAll("&p", playerName);
        m = m.replaceAll("&w", animalName);
        return ChatColor.translateAlternateColorCodes('&', m);
    }
}
