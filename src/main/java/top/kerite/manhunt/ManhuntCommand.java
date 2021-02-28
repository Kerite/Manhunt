package top.kerite.manhunt;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import top.kerite.manhunt.manhuntexception.InvalidRoleException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static top.kerite.manhunt.I18n.tl;

public class ManhuntCommand implements TabExecutor {
    public static final String PERMISSION_JOIN = "manhunt.join";
    public static final String PERMISSION_JOIN_OTHERS = "manhunt.join.others";
    public static final String PERMISSION_START = "manhunt.start";
    public static final String PERMISSION_RELOAD = "manhunt.reload";
    public static final String PERMISSION_CONFIG = "manhunt.config";
    private static final List<String> COMMANDS = Arrays.asList("join", "start", "reload", "list", "verbose", "config");
    private static final List<String> ROLES = Arrays.asList(PlayerRole.RUNNER.getName(), PlayerRole.HUNTER.getName());
    private final Permission perms;
    private final Logger LOGGER;
    private final IManhunt context;
    private final IManhuntMatch match;

    public ManhuntCommand(Permission perms, Logger logger, IManhunt context, IManhuntMatch match) {
        this.perms = perms;
        this.LOGGER = logger;
        this.context = context;
        this.match = match;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, final @NotNull String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command");
        }
        try {
            if (args.length == 0) {
                // print command help
                sender.sendMessage(Color.ORANGE + "Command usage:");
                if (sender.hasPermission(PERMISSION_JOIN)) {
                    sender.sendMessage(Color.ORANGE + " - join [runner|hunter]: join a team");
                }
                if (sender.hasPermission(PERMISSION_START)) {
                    sender.sendMessage(Color.ORANGE + " - start: start a match");
                }
                if (sender.hasPermission(PERMISSION_RELOAD)) {
                    sender.sendMessage(Color.ORANGE + " - reload: reload this  plugins");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("join")) {
                return join(sender, args);
            } else if (args[0].equalsIgnoreCase("start")) {
                return start(sender);
            } else if (args[0].equalsIgnoreCase("reload")) {
                reload(sender);
            } else if (args[0].equalsIgnoreCase("list")) {
                return listMembers(sender, args);
            } else if (args[0].equalsIgnoreCase("verbose")) {
                verbose(sender);
            } else if (args[0].equalsIgnoreCase("config")) {
                config(sender, args);
            } else {
                sender.sendMessage(tl("errorInvalidCommand"));
                return false;
            }
        } catch (InvalidRoleException ire) {
            sender.sendMessage(ire.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("list")) {
                StringUtil.copyPartialMatches(args[1], ROLES, completions);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("join")) {
                List<String> players = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getName()));
                StringUtil.copyPartialMatches(args[2], players, completions);
            }
        }
        return completions;
    }

    private void config(CommandSender sender, String[] args) {
        if (!perms.has(sender, PERMISSION_CONFIG)) {
            sender.sendMessage(ChatColor.RED + "You don't ");
        }
        if (args.length != 3) {
            sender.sendMessage(tl("errorInvalidCommand"));
        } else {
            if (checkConfigAvailable(sender, args[1])) {
                LOGGER.info(args[2]);
                int value;
                try {
                    value = Integer.parseInt(args[2]);
                } catch (NumberFormatException nfe) {
                    sender.sendMessage(tl("errorInvalidArgument"));
                    return;
                }
                context.getFileConfig().set(args[1].toLowerCase(Locale.ROOT), value);
                sender.sendMessage(tl("configChanged", args[1]));
            }
        }
    }

    private boolean checkConfigAvailable(CommandSender sender, String arg) {
        if (arg.equalsIgnoreCase("list")) {
            sender.sendMessage(ManhuntConfig.getConfigList().toString());
            return true;
        } else if (ManhuntConfig.getConfigList().contains(arg)) {
            return true;
        }
        sender.sendMessage(tl("errorConfigItemMissing"));
        return false;
    }

    private boolean join(CommandSender sender, String[] args) throws InvalidRoleException {
        if (args.length != 2 && args.length != 3) {
            sender.sendMessage("Invalid arguments.usage: /manhunt join <hunter|runner> [player]");
            return false;
        }
        if (!perms.has((Player) sender, PERMISSION_JOIN)) {
            sender.sendMessage(tl("errorHasPermission", PERMISSION_JOIN));
            return false;
        }

        Player player;
        if (args.length == 3) {
            if (!perms.has(sender, PERMISSION_JOIN_OTHERS)) {
                sender.sendMessage(tl("errorHasPermission", PERMISSION_JOIN_OTHERS));
                return false;
            }
            player = Bukkit.getServer().getPlayerExact(args[2]);
            if (player == null) {
                sender.sendMessage(tl("playerOffline", args[2]));
                return true;
            }
        } else {
            player = (Player) sender;
        }
        // 添加玩家
        match.join(player, PlayerRole.getRole(args[1]), sender);
        return true;
    }

    private boolean listMembers(CommandSender sender, String[] args) throws InvalidRoleException {
        if (args.length != 2) {
            sender.sendMessage(tl("errorInvalidCommand"));
            return false;
        }
        PlayerRole role = PlayerRole.getRole(args[1]);
        if (role == null) {
            sender.sendMessage(tl("errorInvalidTeam"));
            return false;
        }
        sender.sendMessage("Member list in " + role.getDisplayedName() + ":");
        match.getMembers(role).forEach(player -> sender.sendMessage(" - " + player));
        return true;
    }

    private void reload(CommandSender sender) {
        if (!perms.has(sender, PERMISSION_RELOAD)) {
            sender.sendMessage(tl("errorHasPermission", PERMISSION_RELOAD));
        }
        context.reload();
    }

    private boolean start(CommandSender sender) {
        if (!perms.has(sender, PERMISSION_START)) {
            sender.sendMessage(tl("errorHasPermission", PERMISSION_START));
            return false;
        }
        match.start(sender);
        return true;
    }

    private void verbose(CommandSender sender) {
        sender.sendMessage("---------------------------");
        sender.sendMessage(ChatColor.RED + " - Team: " + sender.getName());
        sender.sendMessage(ChatColor.GREEN + " - Match instance: " + match.toString());
        Player commander = Bukkit.getServer().getPlayer(sender.getName());
        if (commander != null) {
            ItemStack itemStack = commander.getInventory().getItemInMainHand();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                sender.sendMessage(ChatColor.YELLOW + " - Item in the main hand: " + itemStack.getItemMeta().toString());
            }
        }
        sender.sendMessage("---------------------------");
    }
}
