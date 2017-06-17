package eu.crypticcraft.tameprotect.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for entity related tasks.
 */

public class EntityUtils {
    /**
     * Environmental/untrackable damage causes that the plugin will block.
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

    public static Set<EntityDamageEvent.DamageCause> getDamageCauses() {
        return damageCauses;
    }

    /**
     * Gets an entity by its UUID.
     *
     * @param id The UUID of the entity
     * @return The entity object of the found entity, or null if it was not found.
     */
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
        switch (entity.getType()) {
            case HORSE:
                return "Horse";
            case DONKEY:
                return "Donkey";
            case MULE:
                return "Mule";
            case SKELETON_HORSE:
                return "Skeleton Horse";
            case ZOMBIE_HORSE:
                return "Zombie Horse";
            case LLAMA:
                return "Llama";
            case WOLF:
                return "Wolf";
            case OCELOT:
                return "Ocelot";
            default:
                return "Unknown";
        }
    }
}
