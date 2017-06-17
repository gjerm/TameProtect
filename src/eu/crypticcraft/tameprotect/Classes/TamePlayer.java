package eu.crypticcraft.tameprotect.Classes;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * A class to unify the two main Player classes, to simplify fetching.
 */
public class TamePlayer {
    private boolean seen, online;
    private String name;
    private UUID uniqueId;

    /**
     * Constructor using the base Player class.
     * @param player The player to get info from
     */
    public TamePlayer(Player player) {
        seen = player.hasPlayedBefore();
        name = player.getName();
        uniqueId = player.getUniqueId();
        online = player.isOnline();
    }

    /**
     * Constructor using the OfflinePlayer class.
     * @param player The offline player to get info from
     */
    public TamePlayer(OfflinePlayer player) {
        seen = player.hasPlayedBefore();
        name = player.getName();
        uniqueId = player.getUniqueId();
        online = player.isOnline();
    }

    /**
     * @return Whether the player has been seen on this server before.
     */
    public boolean hasPlayedBefore() {
        return seen;
    }

    /**
     * @return Whether the player is currently online.
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * @return The username of the player.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The UUID of the player.
     */
    public UUID getUniqueId() {
        return uniqueId;
    }
}
