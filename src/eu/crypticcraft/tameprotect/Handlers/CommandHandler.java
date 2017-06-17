package eu.crypticcraft.tameprotect.Handlers;
import eu.crypticcraft.tameprotect.Classes.Pair;
import eu.crypticcraft.tameprotect.Protection;
import eu.crypticcraft.tameprotect.TameProtect;
import eu.crypticcraft.tameprotect.Utilities.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.UUID;


public class CommandHandler implements CommandExecutor {
    private TameProtect plugin;

    private HashMap<String, TameTask> commandToTask;
    private enum TameTask {ADD, REMOVE, SET_OWNER, INFO}

    private HashMap<UUID, Pair<TameTask, String>> commandQueue = new HashMap<UUID, Pair<TameTask, String>>();

    public CommandHandler(TameProtect plugin) {
        this.plugin = plugin;
        plugin.getCommand("tame").setExecutor(this);
        plugin.getCommand("tprot").setExecutor(this);
        plugin.getCommand("tameprotect").setExecutor(this);

        commandToTask = new HashMap<String, TameTask>();
        commandToTask.put("add", TameTask.ADD);
        commandToTask.put("remove", TameTask.REMOVE);
        commandToTask.put("setowner", TameTask.SET_OWNER);
        commandToTask.put("info", TameTask.INFO);
    }

    /**
     * Queues a command so that it can be handled when necessary.
     *
     * @param player The UUID of the involved player
     * @param intent The action to be performed
     * @param extraInfo The second argument of the action
     */
    private void queueCommand(final UUID player, TameTask intent, String extraInfo) {
        commandQueue.put(player, new Pair<TameTask, String>(intent, extraInfo));
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                commandQueue.remove(player);
            }
        }, 200L);
    }

    /**
     *
     * @param player The player involved in this event.
     * @param entity The entity involved in this event.
     * @return A boolean indication whether the evoking event should be cancelled or not.
     */
    public boolean onEntityInteract(Player player, Entity entity) {
        Pair<TameTask, String> command = commandQueue.get(player.getUniqueId());
        Protection protection = Protection.loadProtection(entity, plugin);
        if (protection == null || command == null) return false;
        commandQueue.remove(player.getUniqueId());

        final boolean playerIsOwner = protection.getOwner().equals(player.getUniqueId());
        final boolean playerOverride = player.hasPermission("tameprotect.override");

        String action = "";
        switch (command.getKey()) {
            case ADD:
                action = "addmember";
                break;
            case REMOVE:
                action = "removemember";
                break;
            case SET_OWNER:
                action = "setowner";
                break;
            case INFO:
                action = "info";
        }
        final boolean actionPerm = player.hasPermission(String.format("tameprotect.%s", action));
        final boolean actionPermOther = player.hasPermission(String.format("tameprotect.%s.other", action));
        final String argument = command.getValue();
        Player argPlayer = PlayerUtils.getPlayer(argument);

        if ((playerOverride || actionPermOther) || (actionPerm && playerIsOwner)) {
            if (!command.getKey().equals(TameTask.INFO)) {
                Bukkit.broadcastMessage("" + argPlayer.hasPlayedBefore());
                if (argPlayer == null || !argPlayer.hasPlayedBefore()) {
                    player.sendMessage(plugin.getMessage("unknown", "", protection.getName()));
                    return true;
                }
            }
            // argPlayer is already null-checked - IDE may throw warning.
            switch (command.getKey()) {
                case ADD:
                    protection.addMember(argPlayer.getUniqueId());
                    player.sendMessage(plugin.getMessage("add", argPlayer.getName(), protection.getName()));
                    break;
                case REMOVE:
                    protection.removeMember(argPlayer.getUniqueId());
                    player.sendMessage(plugin.getMessage("remove", argPlayer.getName(), protection.getName()));
                    break;
                case SET_OWNER:
                    Player newPlayer = Bukkit.getPlayer(command.getValue());
                    if (newPlayer == null) {
                        player.sendMessage(plugin.getMessage("offline", argPlayer.getName(), protection.getName()));
                        return true;
                    }
                    protection.setOwner(newPlayer);
                    break;
                case INFO:
                    player.sendMessage(protection.getInfo());
                    break;
            }
        }
        else {
            player.sendMessage(plugin.getMessage("permission", argPlayer.getName(), protection.getName()));
        }
        return true;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equals("reload") && sender.hasPermission("tameprotect.reload")) {
                plugin.reloadConfiguration();
                return true;
            }

            final boolean isPlayer = sender instanceof Player;
            if (isPlayer) {
                final Player player = (Player) sender;
                final TameTask queuedType = commandToTask.get(args[0].toLowerCase());
                final String secondArg = args.length > 1 ? args[1] : "";
                if (queuedType != null) {
                    queueCommand(player.getUniqueId(), queuedType, secondArg);
                }
                return true;
            }
        }
        return false;
    }
}
