package eu.crypticcraft.tameprotect.Handlers;
import eu.crypticcraft.tameprotect.Classes.Pair;
import eu.crypticcraft.tameprotect.Classes.TamePlayer;
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

    private long commandTimeout;
    private HashMap<String, TameTask> commandToTask;
    private enum TameTask {ADD, REMOVE, SET_OWNER, INFO}

    private HashMap<UUID, Pair<TameTask, String>> commandQueue = new HashMap<UUID, Pair<TameTask, String>>();

    /**
     * Registers command and sets up some base variables.
     * @param plugin The base plugin instance.
     */
    public CommandHandler(TameProtect plugin) {
        this.commandTimeout = plugin.getConfig().getLong("command_timeout") * 20;
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
     * Reload configuration options.
     */
    public void reload() {
        this.commandTimeout = plugin.getConfig().getLong("command_timeout") * 20;
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
        }, commandTimeout);
    }

    /**
     * To be called whenever a player interacts with an entity. This checks the
     * command queue for relevant actions to perform, and handles everything.
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

        final boolean mountOnCommand = plugin.getConfig().getBoolean("mount_on_command");
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
        TamePlayer argPlayer = PlayerUtils.getPlayer(argument);

        if ((playerOverride || actionPermOther) || (actionPerm && playerIsOwner)) {
            if (!command.getKey().equals(TameTask.INFO)) {
                if (argPlayer == null || (!argPlayer.hasPlayedBefore()) && !argPlayer.isOnline()) {
                    plugin.sendMessage(player, "unknown", "", protection.getName());
                    return !mountOnCommand;
                }
            }
            // argPlayer is already null-checked - IDE may throw warning.
            switch (command.getKey()) {
                case ADD:
                    protection.addMember(argPlayer.getUniqueId());
                    plugin.sendMessage(player, "add", argPlayer.getName(), protection.getName());
                    break;
                case REMOVE:
                    protection.removeMember(argPlayer.getUniqueId());
                    plugin.sendMessage(player, "remove", argPlayer.getName(), protection.getName());
                    break;
                case SET_OWNER:
                    Player newPlayer = Bukkit.getPlayer(command.getValue());
                    if (newPlayer == null) {
                        plugin.sendMessage(player, "offline", argPlayer.getName(), protection.getName());
                        return !mountOnCommand;
                    }
                    protection.setOwner(newPlayer);
                    break;
                case INFO:
                    player.sendMessage(protection.getInfo());
                    break;
            }
        }
        else {
            plugin.sendMessage(player, "permission", argPlayer.getName(), protection.getName());
        }
        return !mountOnCommand;
    }

    /**
     * Handles commands by putting them in a queue.
     *
     * @param sender Sender of command
     * @param command Base command
     * @param label
     * @param args Arguments used
     * @return Whether the command was successfully handled.
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equals("reload") && sender.hasPermission("tameprotect.reload")) {
                plugin.reloadConfiguration();
                plugin.sendMessage((Player) sender, "configuration", "", "");
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
