package eu.crypticcraft.tameprotect;
import eu.crypticcraft.tameprotect.Handlers.DatabaseHandler;
import eu.crypticcraft.tameprotect.Utilities.EntityUtils;
import eu.crypticcraft.tameprotect.Utilities.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

public class Protection {
    private Entity animal;
    private HashSet<UUID> members;
    private DatabaseHandler config;

    /**
     * Constructs a new protection.
     *
     * @param ent
     * @param owner
     * @param config
     */
    public Protection(Entity ent, Player owner, DatabaseHandler config, TameProtect plugin) {
        this.config = config;
        this.animal = ent;
        Tameable tamed = (Tameable) animal;

        // If the animal already has an owner it is imported from a previous tamed animal.
        // In this case, don't set the name of it.
        if (tamed.getOwner() == null && plugin.getConfig().getBoolean("auto_name")) {
            String name = plugin.getConfig().getString("default_name");
            name = name.replaceAll("&p", owner.getName());
            name = name.replaceAll("&w", EntityUtils.getHumanName(ent));
            animal.setCustomNameVisible(true);
            animal.setCustomName(name);
        }

        this.config.createProtection(animal.getUniqueId());
        this.config.saveProtections();

        members = new HashSet<UUID>();
    }

    /**
     * Imports a new protection from the database.
     *
     * @param animalId
     * @param members
     * @param config
     */
    public Protection(UUID animalId, HashSet<UUID> members, DatabaseHandler config) {
        this.config = config;
        animal = EntityUtils.getEntity(animalId);
        if (animal == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Animal added does not exist!");
        }
        this.members = members;
    }

    public UUID getOwner() {
        Tameable tamed = (Tameable) animal;
        return tamed.getOwner().getUniqueId();
    }

    public HashSet<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID memberId) {
        if (!members.contains(memberId)) {
            config.addMember(animal.getUniqueId(), memberId);
            config.saveProtections();
        }
        members.add(memberId);
    }

    public void removeMember(UUID memberId) {
        members.remove(memberId);
        config.removeMember(animal.getUniqueId(), memberId);
        config.saveProtections();
    }


    public boolean setOwner(Player owner, TameProtect plugin) {
        if (animal == null) return false;
        Tameable tamed = (Tameable) animal;

        tamed.setOwner(owner);

        if (plugin.getConfig().getBoolean("auto_name")) {
            String name = plugin.getConfig().getString("default_name");
            name = name.replaceAll("&p", owner.getName());
            name = name.replaceAll("&w", EntityUtils.getHumanName(animal));
            animal.setCustomNameVisible(true);
            animal.setCustomName(name);
        }
        return true;
    }

    public void removeCustomName() {
        animal.setCustomNameVisible(false);
        animal.setCustomName(null);
    }

    public String getName() {
        return EntityUtils.getHumanName(animal);
    }

    public String getFormattedMembers() {
        String members = "";
        int count = 1;
        for (UUID id : this.getMembers()) {
            String newMemberName = PlayerUtils.getPlayerName(id);
            if (newMemberName == null) {
                newMemberName = "Unknown";
            }
            members += newMemberName;
            if (count < this.getMembers().size()) {
                members += ", ";
            }
            count++;
        }
        return members;
    }
    /**
     * Loads a protection. If the animal has an owner and it is not registered with the plugin a new protection will be created.
     *
     * First attempts to find it in the cache. If it does not exist there, it loads it into the cache from the database.
     * @param entity The entity in question.
     * @param plugin The plugin instance.
     * @return The found or created protection, or null if neither apply.
     */
    public static Protection loadProtection(Entity entity, TameProtect plugin) {
        if (!plugin.getConfig().getBoolean("auto_protect")) return null;

        Protection protection;

        if (entity instanceof Tameable) {
            Tameable animal = (Tameable) entity;

            // Irrelevant if animal doesn't have an owner
            if (animal.getOwner() == null) {
                return null;
            }

            // Try loading from plugin cache
            protection = plugin.getProtections().get(entity.getUniqueId());
            if (protection != null) return protection;

            // It was not in the cache, try loading from config
            protection = plugin.getProtectionDatabase().loadProtectionFromConfig(entity.getUniqueId());
            if (protection != null) {
                // It was in config, add to cache
                plugin.getProtections().put(entity.getUniqueId(), protection);
                return protection;
            } else {
                Player owner = (Player) animal.getOwner();
                if (owner.hasPermission("tameprotect.protect")) {
                    // It's not in either but it needs to be registered with the plugin (animal has an owner), add to cache and config
                    protection = new Protection(entity, owner, plugin.getProtectionDatabase(), plugin);
                    plugin.getProtections().put(entity.getUniqueId(), protection);
                    return protection;
                }
            }
        }
        return null;
    }

    public Entity getAnimal() {
        return animal;
    }
}
