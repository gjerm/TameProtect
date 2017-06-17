package eu.crypticcraft.tameprotect;

import eu.crypticcraft.tameprotect.Utils.TameProtectConfigHandler;
import eu.crypticcraft.tameprotect.Utils.TameProtectUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by dfood on 2016-04-30.
 */
public class TameProtection {
    private Entity animal;
    private HashSet<UUID> members;
    private TameProtectConfigHandler config;

    /**
     * Constructs a new protection.
     *
     * @param ent
     * @param owner
     * @param config
     */
    public TameProtection(Entity ent, Player owner, TameProtectConfigHandler config) {
        this.config = config;
        this.animal = ent;
        Tameable tamed = (Tameable) animal;

        // Imported protection? No need to set the owner or change their name
        if (tamed.getOwner() == null) {
            String name = owner.getName() + "'s " + TameProtectUtils.getHumanName(animal);
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
    public TameProtection(UUID animalId, HashSet<UUID> members, TameProtectConfigHandler config) {
        this.config = config;
        animal = TameProtectUtils.getEntity(animalId);
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
        members.add(memberId);
        config.addMember(animal.getUniqueId(), memberId);
        config.saveProtections();
    }

    public void removeMember(UUID memberId) {
        members.remove(memberId);
        config.removeMember(animal.getUniqueId(), memberId);
        config.saveProtections();
    }


    public boolean setOwner(Player owner, World world) {
        if (animal == null) return false;
        Tameable tamed = (Tameable) animal;

        tamed.setOwner(owner);

        animal.setCustomName(owner.getName() + "'s " + TameProtectUtils.getHumanName(animal));
        return true;
    }

    public boolean dealEnvironmentalDamage(boolean ridingCausesEnvDamage, EntityDamageEvent.DamageCause cause) {
        List<Entity> riding = this.animal.getPassengers();
        if (!TameProtectUtils.getDamageCauses().contains(cause) || !(ridingCausesEnvDamage && riding.size() > 0))
            return false;
        return true;
    }

    public String getInfo() {
        String members = "";
        int count = 1;
        for (UUID id : this.getMembers()) {
            String newMemberName = TameProtectUtils.getPlayerName(id);
            if (newMemberName == null) {
                newMemberName = "Unknown";
            }
            members += newMemberName;
            if (count < this.getMembers().size()) {
                members += ", ";
            }
            count++;
        }
        return "Owner: " + TameProtectUtils.getPlayerName(this.getOwner()) + "\n" + "Members: " + members;
    }

}
