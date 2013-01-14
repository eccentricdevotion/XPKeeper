package me.eccentric_nz.plugins.xpkeeper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class XPKexecutor extends JavaPlugin implements CommandExecutor {

    private Xpkeeper plugin;
    private XPKdatabase service = XPKdatabase.getInstance();

    public XPKexecutor(Xpkeeper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("xpkgive")) {
            if (!sender.hasPermission("xpkeeper.admin")) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " You do not have permission to use that command!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Not enough command arguments!");
                return false;
            }
            if (plugin.getServer().getPlayer(args[0]) == null) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Could not find that player!");
                return true;
            }
            Player player = plugin.getServer().getPlayer(args[0]);
            XPKCalculator xpkc = new XPKCalculator(player);
            int i = 0;
            try {
                i = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.err.println("[XPKeeper] could not convert to number!");
            }
            xpkc.changeExp(i);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkset")) {
            if (!sender.hasPermission("xpkeeper.admin")) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " You do not have permission to use that command!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Not enough command arguments!");
                return false;
            }
            if (plugin.getServer().getPlayer(args[0]) == null) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Could not find that player!");
                return true;
            }
            Player player = plugin.getServer().getPlayer(args[0]);
            XPKCalculator xpkc = new XPKCalculator(player);
            int i = 0;
            try {
                i = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.err.println("[XPKeeper] could not convert to number!");
            }
            xpkc.setExp(i);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkremove")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.trackPlayers.put(player.getName(), true);
                player.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Click the XPKeeper sign you wish to remove.");
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("xpkforceremove")) {
            String player;
            if (args.length == 1 && sender.hasPermission("xpkeeper.force")) {
                player = args[0];
            } else {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    player = p.getName();
                } else {
                    sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " You must specify a player name when running this command from the console.");
                    return true;
                }
            }
            Statement statement = null;
            ResultSet rsget = null;
            try {
                Connection connection = service.getConnection();
                statement = connection.createStatement();
                String queryRemoveGet = "SELECT xpk_id FROM xpk WHERE player = '" + player + "'";
                rsget = statement.executeQuery(queryRemoveGet);
                if (rsget.isBeforeFirst()) {
                    String queryRemovePlayer = "DELETE FROM xpk WHERE player = '" + player + "'";
                    statement.executeUpdate(queryRemovePlayer);
                }
            } catch (SQLException e) {
                System.err.println("[XPKeeper] Could not get and remove player data: " + e);
            } finally {
                if (rsget != null) {
                    try {
                        rsget.close();
                    } catch (Exception e) {
                    }
                }
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception e) {
                    }
                }
            }
            sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " All database entries for " + ChatColor.RED + player + ChatColor.RESET + " were removed.");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkfist")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("xpkeeper.fist")) {
                    player.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " You do not have permission to use that command!");
                    return true;
                }
            }
            boolean bool = plugin.getConfig().getBoolean("must_use_fist");
            plugin.getConfig().set("must_use_fist", !bool);
            plugin.saveConfig();
            sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " XPKeeper must_use_fist config set to:" + !bool);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkedit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Silly console, you can't change signs!");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("xpkeeper.editsign")) {
                player.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + "You don't have permission to edit XPKeeper signs!");
                return true;
            }
            Sign sign;
            try {
                sign = (Sign) player.getTargetBlock(null, 10).getState();
            } catch (NullPointerException ex) {
                player.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + "Couldn't find the sign, maybe you're too far away.");
                return true;
            } catch (ClassCastException ex) {
                player.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + "You aren't looking at a sign!");
                return true;
            }
            StringBuilder builder = new StringBuilder();
            for (String a : args) {
                builder.append(a).append(" ");
            }
            String newline = builder.toString();
            // remove trailing space
            String trimmed = newline.substring(0, newline.length() - 1);
            sign.setLine(0, trimmed);
            sign.update();
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("xpkpay")) {
            if (!sender.hasPermission("xpkeeper.use")) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " You do not have permission to use that command!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Not enough command arguments!");
                return false;
            }
            if (plugin.getServer().getPlayer(args[0]) == null) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Could not find that player!");
                return true;
            }
            Player giver = null;
            if (sender instanceof Player) {
                giver = (Player) sender;
            }
            if (giver == null) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Only players can pay other players!");
                return true;
            }
            Player receiver = plugin.getServer().getPlayer(args[0]);
            XPKCalculator xpkc_g = new XPKCalculator(giver);
            XPKCalculator xpkc_r = new XPKCalculator(receiver);
            int i = 0;
            try {
                i = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                sender.sendMessage("[XPKeeper] could not convert to number!");
                return false;
            }
            // check whether the giver has enough to give
            int checkEnough = xpkc_g.getCurrentExp();
            if (i > checkEnough) {
                sender.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " You don't have enough XP! Try withdrawing from your XPKeeper sign first.");
                return true;
            }
            xpkc_r.changeExp(i);
            xpkc_g.changeExp(-i);
            giver.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " You payed " + args[0] + " " + args[1] + " XP :)");
            receiver.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + giver.getName()+ " payed you " + args[1] + " XP :)");
            return true;
        }
        return false;
    }
}