package eu.crypticcraft.tameprotect.Utils;

import eu.crypticcraft.tameprotect.TameProtect;
import eu.crypticcraft.tameprotect.TameProtection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;
import java.util.logging.Level;


/**
 * Created by dfood on 2016-05-01.
 */
public class TameProtectUtils {
    /**
     * The damage causes that will be blocked by the plugin.
     */

    private static final Set<EntityDamageEvent.DamageCause> damageCauses = new HashSet<EntityDamageEvent.DamageCause>(Arrays.asList(EntityDamageEvent.DamageCause.SUICIDE,
            EntityDamageEvent.DamageCause.STARVATION, EntityDamageEvent.DamageCause.VOID,
            EntityDamageEvent.DamageCause.SUFFOCATION, EntityDamageEvent.DamageCause.FALL,
            EntityDamageEvent.DamageCause.DROWNING, EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
            EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, EntityDamageEvent.DamageCause.LIGHTNING,
            EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.CONTACT,
            EntityDamageEvent.DamageCause.FALLING_BLOCK, EntityDamageEvent.DamageCause.POISON,
            EntityDamageEvent.DamageCause.MAGIC, EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK));

    /**
     * Find a player by name.
     *
     * @param name Name of player.
     * @return A pair consisting of the UUID and name (in case the input had wrong capitalisation)
     */
    public static Pair<UUID, String> getPlayer(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            if (offlinePlayer == null) {
                return null;
            } else {
                return new Pair<UUID, String>(offlinePlayer.getUniqueId(), offlinePlayer.getName());
            }
        } else {
            return new Pair<UUID, String>(player.getUniqueId(), player.getName());
        }
    }

    /**
     * Get a player's name by their UUID.
     * @param id The UUID.
     * @return The name of the player found.
     */

    public static String getPlayerName(UUID id) {
        Player player = Bukkit.getPlayer(id);
        if (player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
            if (offlinePlayer == null) {
                return null;
            } else {
                return offlinePlayer.getName();
            }
        } else {
            return player.getName();
        }
    }

    public static Set<EntityDamageEvent.DamageCause> getDamageCauses() {
        return damageCauses;
    }

    public static Entity getEntity(UUID id) {
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e.getUniqueId().equals(id)) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Returns the human-readable name for an entity provided it is rideable.
     *
     * @param entity The entity to obtain the name of.
     * @return The human-readable name of the entity.
     */
    public static String getHumanName(Entity entity) {
        if (entity.getType().equals(EntityType.HORSE)) {
            return "Horse";
        } else if (entity.getType().equals(EntityType.DONKEY)) {
            return "Donkey";
        } else if (entity.getType().equals(EntityType.MULE)) {
            return "Mule";
        } else if (entity.getType().equals(EntityType.SKELETON_HORSE)) {
            return "Skeleton Horse";
        } else if (entity.getType().equals(EntityType.ZOMBIE_HORSE)) {
            return "Zombie Horse";
        } else if (entity.getType().equals(EntityType.LLAMA)) {
            return "Llama";
        } else if (entity.getType().equals(EntityType.WOLF)) {
            return "Wolf";
        } else if (entity.getType().equals(EntityType.OCELOT)) {
            return "Ocelot";
        }
        return null;
    }

    /**
     * Load all protections from the database.
     *
     * @param configHandler The handler for this, to get the actual database.
     * @return A HashMap of all the loaded protections.
     */
    public static HashMap<UUID, TameProtection> getFromConfig(TameProtectConfigHandler configHandler) {
        FileConfiguration configuration = configHandler.getProtectionConfig();
        HashMap<UUID, TameProtection> convertedProtections = new HashMap<UUID, TameProtection>();
        Set<String> ids = configuration.getKeys(false);
        for (String id : ids) {
            UUID animalId = UUID.fromString(id);
            List<String> memberList = configuration.getStringList(id + ".members");
            HashSet<UUID> members = new HashSet<UUID>();

            for (String m : memberList) {
                UUID member = UUID.fromString(m);
                members.add(member);
            }

            TameProtection prot = new TameProtection(animalId, members, configHandler);
            convertedProtections.put(animalId, prot);
        }
        return convertedProtections;
    }

    /**
     * Loads a protection. If the animal has an owner and it is not registered with the plugin a new protection will be created.
     *
     * First attempts to find it in the cache. If it does not exist there, it loads it into the cache from the database.
     * @param entity The entity in question.
     * @param plugin The plugin instance.
     * @return The newly created (or not) protection.
     */
    public static TameProtection loadProtection(Entity entity, TameProtect plugin) {
        TameProtection protection;

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
            protection = plugin.getConfigProtections().loadProtectionFromConfig(entity.getUniqueId());
            if (protection != null) {
                // It was in config, add to cache
                plugin.getProtections().put(entity.getUniqueId(), protection);
                return protection;
            } else {
                Player owner = (Player) animal.getOwner();
                if (owner.hasPermission("tameprotect.protect")) {
                    // It's not in either but it needs to be registered with the plugin (animal has an owner), add to cache and config
                    protection = new TameProtection(entity, owner, plugin.getConfigProtections());
                    plugin.getProtections().put(entity.getUniqueId(), protection);
                    return protection;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param command
     * @param protection
     * @param player
     * @param plugin
     * @return
     */
    public static boolean commandHandler(Pair<String, String> command, TameProtection protection, Player player, TameProtect plugin) {
        if (command != null) {
            // Command will execute, so remove from queue.
            plugin.getCommandQueue().remove(player.getUniqueId());

            boolean playerIsOwner = protection.getOwner().equals(player.getUniqueId());
            String cmd = command.getKey();
            String arg = command.getValue();

            if (cmd.equalsIgnoreCase("info") && player.hasPermission("tameprotect.info")) {
                player.sendMessage(protection.getInfo());
                return true;
            }

            else if (cmd.equalsIgnoreCase("add") && player.hasPermission("tameprotect.addmember")) {
                Pair<UUID, String> toAdd = getPlayer(arg);
                if (toAdd == null) {
                    player.sendMessage("This player does not exist!");
                    return true;
                }

                if (playerIsOwner || player.hasPermission("tameprotect.addmember.other")) {
                    protection.addMember(toAdd.getKey());
                    player.sendMessage(toAdd.getValue() + " was added to this protection.");
                    return true;
                }
                else {
                    player.sendMessage("You are not allowed to add members to this animal!");
                    return true;
                }

            }

            else if (cmd.equalsIgnoreCase("remove") && player.hasPermission("tameprotect.removemember")) {
                Pair<UUID, String> toRemove = getPlayer(arg);
                if (toRemove == null) {
                    player.sendMessage("This player does not exist!");
                    return true;
                }

                if (protection.getOwner().equals(player.getUniqueId()) || player.hasPermission("tameprotect.removemember.other")) {
                    protection.removeMember(toRemove.getKey());
                    player.sendMessage(toRemove.getValue() + " was removed from this protection.");
                    return true;
                }
                else {
                    player.sendMessage("You are not allowed to remove members of this animal!");
                    return true;
                }
            }

            else if (command.getKey().equalsIgnoreCase("setowner") && player.hasPermission("tameprotect.setowner")) {
                Player newOwner = Bukkit.getPlayer(command.getValue());
                if (newOwner == null) {
                    player.sendMessage("This player does not exist or is offline!");
                    return true;
                }

                // The person doing this must be the owner or have the permission to set owner for other peoples animals.
                if (protection.getOwner().equals(player.getUniqueId()) || player.hasPermission("tameprotect.setowner.other")) {
                    if (!protection.setOwner(newOwner, player.getWorld())) {
                        player.sendMessage("Unable to set new owner!");
                        return true;
                    }
                    else {
                        player.sendMessage("Successfully changed owner.");
                    }
                }
                else {
                    player.sendMessage("You're not allowed to set the owner of this animal!");
                }
            }

            else {
                player.sendMessage("Invalid command or not allowed!");
            }

        }
        return false;
    }
}
