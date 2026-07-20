package me.eccentric_nz.xpkeeper;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;

public class XPKExecutor implements CommandExecutor {

    public final HashMap<String, String> colours;
    private final XPKeeper plugin;
    private final XPKDatabase service = XPKDatabase.getInstance();

    public XPKExecutor(XPKeeper plugin) {
        this.plugin = plugin;
        colours = new HashMap<>();
        colours.put("&0", "Black");
        colours.put("&1", "Dark Blue");
        colours.put("&2", "Dark Green");
        colours.put("&3", "Dark Aqua");
        colours.put("&4", "Dark Red");
        colours.put("&5", "Purple");
        colours.put("&6", "Gold");
        colours.put("&7", "Grey");
        colours.put("&8", "Dark Grey");
        colours.put("&9", "Indigo");
        colours.put("&a", "Bright Green");
        colours.put("&b", "Aqua");
        colours.put("&c", "Red");
        colours.put("&d", "Pink");
        colours.put("&e", "Yellow");
        colours.put("&f", "White");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("xpkreload")) {
            if (!sender.hasPermission("xpkeeper.admin")) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.no_perms_command"));
                return true;
            }
            plugin.reloadConfig();
            XPKUtils.xpkMessage(sender, "Config reloaded!");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkgive")) {
            if (!sender.hasPermission("xpkeeper.admin")) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.no_perms_command"));
                return true;
            }
            if (args.length < 2) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.arguments"));
                return false;
            }
            Player player = plugin.getServer().getPlayer(args[0]);
            if (player == null) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.no_player"));
                return true;
            }
            XPKCalculator xpkc = new XPKCalculator(player);
            int i = 0;
            try {
                i = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                plugin.getLogger().log(Level.INFO, "Could not convert to number!");
            }
            xpkc.changeExp(i);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkset")) {
            if (!sender.hasPermission("xpkeeper.admin")) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.no_perms_command"));
                return true;
            }
            if (args.length < 2) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.arguments"));
                return false;
            }
            if (plugin.getServer().getPlayer(args[0]) == null) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.no_player"));
                return true;
            }
            Player player = plugin.getServer().getPlayer(args[0]);
            XPKCalculator xpkc = new XPKCalculator(player);
            int i = 0;
            try {
                i = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                plugin.getLogger().log(Level.INFO, "Could not convert to number!");
            }
            xpkc.setExp(i);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkremove")) {
            if (sender instanceof Player player) {
                plugin.trackPlayers.add(player.getUniqueId());
                XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.click_sign"));
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("xpkforceremove")) {
            Player p = null;
            if (sender instanceof Player) {
                p = (Player) sender;
            }
            String uuid;
            String name;
            if (args.length == 0) {
                if (sender instanceof Player && p != null) {
                    uuid = p.getUniqueId().toString();
                    name = p.getName();
                } else {
                    XPKUtils.xpkMessage(sender, "You must specify a player name when running this command from the console.");
                    return true;
                }
            } else {
                if (!sender.hasPermission("xpkeeper.force")) {
                    XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.no_perms_command"));
                    return true;
                } else {
                    OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[0]);
                    if (player == null) {
                        XPKUtils.xpkMessage(sender, "Player not found!");
                        return true;
                    }
                    name = args[0];
                    uuid = player.getUniqueId().toString();
                }
            }
            Statement statement = null;
            ResultSet rsget = null;
            try {
                Connection connection = service.getConnection();
                statement = connection.createStatement();
                String queryRemoveGet = "SELECT xpk_id FROM xpk WHERE uuid = '" + uuid + "'";
                rsget = statement.executeQuery(queryRemoveGet);
                if (rsget.isBeforeFirst()) {
                    String queryRemovePlayer = "DELETE FROM xpk WHERE uuid = '" + uuid + "'";
                    statement.executeUpdate(queryRemovePlayer);
                    XPKUtils.xpkMessage(sender, "All database entries for " + name + " were removed.");
                    if (p != null) {
                        plugin.trackOps.add(p.getUniqueId());
                        XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.click_sign"));
                    }
                    return true;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.INFO, "Could not get and remove player data: " + e);
            } finally {
                try {
                    if (rsget != null) {
                        rsget.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.INFO, "Could not close result set/statement when removing player data: " + e);
                }
            }
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("xpkfist")) {
            if (sender instanceof Player player) {
                if (!player.hasPermission("xpkeeper.fist")) {
                    XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.no_perms_command"));
                    return true;
                }
            }
            boolean bool = plugin.getConfig().getBoolean("must_use_fist");
            plugin.getConfig().set("must_use_fist", !bool);
            plugin.saveConfig();
            XPKUtils.xpkMessage(sender, "must_use_fist config value set to: " + !bool);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpklimit")) {
            if (sender instanceof Player player) {
                if (!player.hasPermission("xpkeeper.limit")) {
                    XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.no_perms_command"));
                    return true;
                }
            }
            boolean bool = plugin.getConfig().getBoolean("set_limits");
            plugin.getConfig().set("set_limits", !bool);
            plugin.saveConfig();
            XPKUtils.xpkMessage(sender, "set_limits config value set to: " + !bool);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkwithdraw")) {
            if (sender instanceof Player player) {
                if (!player.hasPermission("xpkeeper.admin")) {
                    XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.no_perms_command"));
                    return true;
                }
            }
            if (args.length < 1) {
                XPKUtils.xpkMessage(sender, " You must specify a number");
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                XPKUtils.xpkMessage(sender, " That is not a number!");
                return false;
            }
            plugin.getConfig().set("withdraw", amount);
            plugin.saveConfig();
            XPKUtils.xpkMessage(sender, " withdraw config value set to: " + amount);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkcolour")) {
            if (sender instanceof Player player) {
                if (!player.hasPermission("xpkeeper.admin")) {
                    XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.no_perms_command"));
                    return true;
                }
            }
            if (args.length < 1) {
                XPKUtils.xpkMessage(sender, " You must specify a colour code like this: &6");
                return true;
            }
            String c = args[0].toLowerCase();
            if (!colours.containsKey(c)) {
                XPKUtils.xpkMessage(sender, " You must specify a colour code like this: &6");
                return true;
            }
            plugin.getConfig().set("firstline_colour", c);
            plugin.saveConfig();
            XPKUtils.xpkMessage(sender, " firstline_colour config value set to: " + colours.get(c));
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkedit")) {
            if (!(sender instanceof Player)) {
                XPKUtils.xpkMessage(sender, "Silly console, you can't change signs!");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("xpkeeper.editsign")) {
                XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.no_perms_command"));
                return true;
            }
            Sign sign;
            try {
                sign = (Sign) player.getTargetBlock(null, 10).getState();
            } catch (NullPointerException ex) {
                XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.no_sign"));
                return true;
            } catch (ClassCastException ex) {
                XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.look_sign"));
                return true;
            }
            StringBuilder builder = new StringBuilder();
            for (String a : args) {
                builder.append(a).append(" ");
            }
            String newline = builder.toString();
            // remove trailing space
            String trimmed = newline.substring(0, newline.length() - 1);
            XPKWriteSign.update(sign, trimmed);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkpay")) {
            if (!sender.hasPermission("xpkeeper.pay")) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.no_perms_command"));
                return true;
            }
            if (args.length < 2) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.arguments"));
                return false;
            }
            if (plugin.getServer().getPlayer(args[0]) == null) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.no_player"));
                return true;
            }
            Player giver = null;
            if (sender instanceof Player) {
                giver = (Player) sender;
            }
            if (giver == null) {
                XPKUtils.xpkMessage(sender, "Only players can pay other players!");
                return true;
            }
            Player receiver = plugin.getServer().getPlayer(args[0]);
            XPKCalculator xpkc_g = new XPKCalculator(giver);
            XPKCalculator xpkc_r = new XPKCalculator(receiver);
            int i;
            try {
                i = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                sender.sendMessage("[XPKeeper] could not convert to number!");
                return false;
            }
            // check whether the giver has enough to give
            double checkEnough = xpkc_g.getCurrentExp();
            if (i > checkEnough) {
                XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.not_enough"));
                return true;
            }
            xpkc_r.changeExp(i);
            xpkc_g.changeExp(-i);
            XPKUtils.xpkMessage(giver, String.format(plugin.getConfig().getString("messages.giver"), args[0], i));
            XPKUtils.xpkMessage(receiver, String.format(plugin.getConfig().getString("messages.reciever"), giver.getName(), i));
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkupdate")) {
            if (sender instanceof Player player) {
                String world = args.length > 0 ? args[0]: null;
                plugin.trackUpdaters.put(player.getUniqueId(), world);
                XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.update_sign"));
                return true;
            }
        }
        return false;
    }
}
