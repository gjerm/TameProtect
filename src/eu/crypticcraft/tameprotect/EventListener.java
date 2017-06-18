package eu.crypticcraft.tameprotect;

import eu.crypticcraft.tameprotect.Classes.MessageInfo;
import eu.crypticcraft.tameprotect.Utilities.EntityUtils;
import eu.crypticcraft.tameprotect.Utilities.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;


public class EventListener implements Listener {
    private TameProtect plugin;

    public EventListener(TameProtect plugin) { this.plugin = plugin; }

    // Tame events can fire more than one time, keep track of recent events.
    private HashSet<UUID> tameOut = new HashSet<UUID>();

    /**
     * As the tame event can fire multiple times for a single tame event,
     * this method temporarily adds a player to a queue so the event isn't
     * handled multiple times.
     *
     * @param player The player to ignore tame events for a certain time for.
     */
    public void tameOut(final UUID player) {
        tameOut.add(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                tameOut.remove(player);
            }
        }, 20L);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTameAnimal (EntityTameEvent event) {
        if (!plugin.getConfig().getBoolean("auto_protect")) return;
        if (event.getOwner() instanceof Player) {
            Player owner = (Player) event.getOwner();
            if (owner.hasPermission("tameprotect.protect")) {
                // If player already tamed within the last second, ignore (TameEvent can fire multiple times, setting the name wrong)
                if (tameOut.contains(owner.getUniqueId())) return;

                Protection protection = new Protection(event.getEntity(), (Player) event.getOwner(), plugin.getProtectionDatabase(), plugin);
                plugin.getProtections().put(event.getEntity().getUniqueId(), protection);
                tameOut(owner.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMountAnimal (VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player && event.getVehicle() instanceof Tameable) {
            Player player = (Player) event.getEntered();
            if (player.hasPermission("tameprotect.override")) return;

            Protection protection = Protection.loadProtection(event.getVehicle(), plugin);

            // Nobody owns this animal
            if (protection == null) {
                return;
            }

            MessageInfo msgInfo = new MessageInfo();
            msgInfo.animalName = protection.getName();
            msgInfo.playerName = player.getName();

            // Player is not the owner nor are they a member of the horse protection
            if (!player.getUniqueId().equals(protection.getOwner()) && !protection.getMembers().contains(player.getUniqueId())) {
                plugin.sendMessage(player, "ride", msgInfo);
                event.setCancelled(true);
            }
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath (EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Tameable)) return;

        Protection protection = Protection.loadProtection(event.getEntity(), plugin);

        if (protection == null) return;

        UUID entityId = event.getEntity().getUniqueId();

        // Remove a protection on animal death if it exists.
        plugin.getProtections().remove(entityId);
        plugin.getProtectionDatabase().removeProtection(entityId);

    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage (EntityDamageByEntityEvent event) {
        // Don't care for unrelated entities
        if (!(event.getEntity() instanceof Tameable)) return;

        Protection protection = Protection.loadProtection(event.getEntity(), plugin);
        if (protection == null) return;

        MessageInfo msgInfo = new MessageInfo();
        msgInfo.animalName = protection.getName();
        msgInfo.playerName = PlayerUtils.getPlayerName(protection.getOwner());

        Player damagingPlayer = null;
        if (event.getDamager() instanceof Player) {
            damagingPlayer = (Player) event.getDamager();
        }
        else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                damagingPlayer = (Player) arrow.getShooter();
            }
        }

        if (damagingPlayer != null) {
            if (!(protection.getOwner().equals(damagingPlayer.getUniqueId()) || damagingPlayer.hasPermission("tameprotect.override"))) {
                plugin.sendMessage(damagingPlayer, "harm", msgInfo);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnvDamage (EntityDamageEvent event) {
        // Ensure the entitytype is what we need and the animal has a protection.
        if (!(event.getEntity() instanceof Tameable)) return;
        Protection protection = Protection.loadProtection(event.getEntity(), plugin);
        if (protection == null) return;

        // If the plugin is configured to allow environmental damage, just pass it on like normal
        if (plugin.getConfig().getBoolean("environmental_damage")) return;

        // If someone is riding, don't cancel the damage event
        List<Entity> riding = event.getEntity().getPassengers();
        if (plugin.getConfig().getBoolean("damage_while_riding") && riding.size() > 0) return;
        if (EntityUtils.getDamageCauses().contains(event.getCause())) {
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityInteract (PlayerInteractEntityEvent event) {
        final boolean result = plugin.getCommandHandler().onEntityInteract(event.getPlayer(), event.getRightClicked());
        event.setCancelled(result);
    }
}
