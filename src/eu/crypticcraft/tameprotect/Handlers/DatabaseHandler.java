package eu.crypticcraft.tameprotect.Handlers;

import eu.crypticcraft.tameprotect.Protection;
import eu.crypticcraft.tameprotect.TameProtect;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Class to handle saving protections into the flatfile database.
 */
public class DatabaseHandler {
    private FileConfiguration protectionConfig;
    private File savedProtections;
    private TameProtect plugin;

    /**
     * Constructor for the database handler
     * @param plugin The base plugin instance
     */
    public DatabaseHandler(TameProtect plugin) {
        this.plugin = plugin;
        reloadProtections();

        // Save the protections database every minute
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                saveProtections();
            }
        }, 0L, plugin.getConfig().getLong("save_interval") * 20);
    }

    /**
     * Reload all protections from the database file.
     */
    public void reloadProtections() {
        if (savedProtections == null) {
            savedProtections = new File(plugin.getDataFolder() + "/protections.yml");
        }
        protectionConfig = YamlConfiguration.loadConfiguration(savedProtections);
        if (!protectionConfig.contains("migrated")) {
            // Set an option in case we want to convert the database to for example SQL at a later time.
            protectionConfig.set("migrated", false);
        }
    }

    /**
     * Save all protections to the database file.
     */
    public void saveProtections() {
        try {
            protectionConfig.save(savedProtections);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Couldn't save protections!");
        }
    }

    /**
     * Load a single protection from the database.
     *
     * @param animalId The UUID of the animal.
     * @return A TameProtection instance of the loaded protection or null.
     */
    public Protection loadProtectionFromConfig (UUID animalId) {
        Set<String> ids = protectionConfig.getKeys(false);
        String animalIdString = animalId.toString();

        if (ids.contains(animalIdString)) {
            List<String> memberList = protectionConfig.getStringList(animalIdString + ".members");
            HashSet<UUID> members = new HashSet<UUID>();

            for (String m : memberList) {
                UUID member = UUID.fromString(m);
                members.add(member);
            }

            return new Protection(animalId, members, this);
        }
        else {
            return null;
        }
    }

    /**
     * Add a member to the database.
     * @param animalId The UUID of the tameable entity
     * @param memberId the UUID of the player to be added
     */
    public void addMember (UUID animalId, UUID memberId) {
        List<String> oldList = protectionConfig.getStringList(animalId.toString() + ".members");
        oldList.add(memberId.toString());
        protectionConfig.set(animalId.toString()+ ".members", oldList);
    }

    /**
     * Remove a member from the database.
     * @param animalId The UUID of the tameable entity
     * @param memberId the UUID of the player to be removed
     */
    public void removeMember (UUID animalId, UUID memberId) {
        List<String> oldList = protectionConfig.getStringList(animalId.toString() + ".members");
        oldList.remove(memberId.toString());
        protectionConfig.set(animalId.toString()+ ".members", oldList);
    }

    /**
     * Create a protection in the database.
     * @param animalId The UUID of the tameable entity
     */
    public void createProtection(UUID animalId) {
        protectionConfig.set(animalId.toString() + ".members", new LinkedList<String>());
    }

    /**
     * Remove a protection from the database.
     * @param animalId The UUID of the tameable entity
     */
    public void removeProtection(UUID animalId) {
        protectionConfig.set(animalId.toString(), null);
    }
}
