package eu.crypticcraft.tameprotect;

import eu.crypticcraft.tameprotect.Utils.Pair;
import eu.crypticcraft.tameprotect.Utils.TameProtectUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.List;
import java.util.UUID;


public class TameProtectListener implements Listener {
    private TameProtect plugin;

    public TameProtectListener(TameProtect plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTameAnimal (EntityTameEvent event) {
        if (event.getOwner() instanceof Player) {
            Player owner = (Player) event.getOwner();
            if (owner.hasPermission("tameprotect.protect")) {
                // If player already tamed within the last half second, ignore (TameEvent can fire multiple times, setting the name wrong)
                if (plugin.getTimeOut().contains(owner.getUniqueId())) return;

                TameProtection protection = new TameProtection(event.getEntity(), (Player) event.getOwner(), plugin.getConfigProtections());
                plugin.getProtections().put(event.getEntity().getUniqueId(), protection);
                plugin.tameOut(owner.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMountAnimal (VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player && event.getVehicle() instanceof Tameable) {
            Player player = (Player) event.getEntered();
            TameProtection protection = TameProtectUtils.loadProtection(event.getEntered(), plugin);

            // Nobody owns this animal
            if (protection == null) {
                return;
            }
            Bukkit.broadcastMessage(player.getDisplayName());
            // Player is not the owner nor are they a member of the horse protection
            if (!player.getUniqueId().equals(protection.getOwner()) && !protection.getMembers().contains(player.getUniqueId())) {
                player.sendMessage(plugin.getConfig().getString("messages.cannot_ride"));
                event.setCancelled(true);
            }
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath (EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Tameable)) return;

        UUID entityId = event.getEntity().getUniqueId();
        TameProtection protection = plugin.getProtections().get(entityId);

        if (protection == null) {
            protection = plugin.getConfigProtections().loadProtectionFromConfig(event.getEntity().getUniqueId());
        }

        // Remove a protection on animal death if it exists.
        if (protection != null) {
            plugin.getProtections().remove(entityId);
            plugin.getConfigProtections().removeProtection(entityId);
            plugin.getConfigProtections().saveProtections();
        }
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage (EntityDamageByEntityEvent event) {
        // Don't care for unrelated entities
        if (!(event.getEntity() instanceof Tameable)) return;

        TameProtection protection = TameProtectUtils.loadProtection(event.getEntity(), plugin);
        if (protection == null) return;

        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (!(protection.getOwner().equals(player.getUniqueId()) || player.hasPermission("tameprotect.override"))) {
                player.sendMessage(plugin.getConfig().getString("messages.cannot_harm"));
                event.setCancelled(true);
            }
        }
        else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                if (protection.getOwner().equals(shooter.getUniqueId()) || shooter.hasPermission("tameprotect.override")) {
                    shooter.sendMessage(plugin.getConfig().getString("messages.cannot_harm"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnvDamage (EntityDamageEvent event) {
        // Ensure the entitytype is what we need and the animal has a protection.
        if (!(event.getEntity() instanceof Tameable)) return;
        TameProtection protection = TameProtectUtils.loadProtection(event.getEntity(), plugin);
        if (protection == null) return;


        // If someone is riding, don't cancel the damage event
        List<Entity> riding = event.getEntity().getPassengers();
        if (plugin.getConfig().getBoolean("damage_while_riding") && riding.size() > 0) return;
        if (TameProtectUtils.getDamageCauses().contains(event.getCause())) {
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityInteract (PlayerInteractEntityEvent event) {
        TameProtection protection = TameProtectUtils.loadProtection(event.getRightClicked(), plugin);
        if (protection == null) return;

        Pair<String, String> command = plugin.getCommandQueue().get(event.getPlayer().getUniqueId());
        if (TameProtectUtils.commandHandler(command, protection, event.getPlayer(), plugin)) {
            event.setCancelled(true);
        }
    }
}
