package eu.crypticcraft.tameprotect.Utils;

import eu.crypticcraft.tameprotect.TameProtect;
import eu.crypticcraft.tameprotect.TameProtection;
import eu.crypticcraft.tameprotect.Utils.TameProtectUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by dfood on 08.06.2016.
 */
public class TameProtectConfigHandler {
    private FileConfiguration protectionConfig;
    private File savedProtections;
    private TameProtect plugin;

    public TameProtectConfigHandler(TameProtect plugin) {
        this.plugin = plugin;
        reloadProtections();
    }

    public FileConfiguration getProtectionConfig() {
        return this.protectionConfig;
    }

    public void reloadProtections() {
        if (savedProtections == null) {
            savedProtections = new File(plugin.getDataFolder() + "/protections.yml");
        }
        protectionConfig = YamlConfiguration.loadConfiguration(savedProtections);
    }

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
    public TameProtection loadProtectionFromConfig (UUID animalId) {
        Set<String> ids = protectionConfig.getKeys(false);
        String animalIdString = animalId.toString();

        if (ids.contains(animalIdString)) {
            List<String> memberList = protectionConfig.getStringList(animalIdString + ".members");
            HashSet<UUID> members = new HashSet<UUID>();

            for (String m : memberList) {
                UUID member = UUID.fromString(m);
                members.add(member);
            }

            return new TameProtection(animalId, members, this);
        }
        else {
            return null;
        }
    }


    public void addMember (UUID animalId, UUID memberId) {
        List<String> oldList = protectionConfig.getStringList(animalId.toString() + ".members");
        oldList.add(memberId.toString());
        protectionConfig.set(animalId.toString()+ ".members", oldList);
    }

    public void removeMember (UUID animalId, UUID memberId) {
        List<String> oldList = protectionConfig.getStringList(animalId.toString() + ".members");
        oldList.remove(memberId.toString());
        protectionConfig.set(animalId.toString()+ ".members", oldList);
    }

    public void createProtection(UUID animalId) {
        protectionConfig.set(animalId.toString() + ".members", new LinkedList<String>());
    }

    public void removeProtection(UUID animalId) {
        protectionConfig.set(animalId.toString(), null);
    }

}
