package me.eccentric_nz.plugins.xpkeeper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class XPKexecutor extends JavaPlugin implements CommandExecutor {

    private Xpkeeper plugin;
    XPKdatabase service = XPKdatabase.getInstance();

    public XPKexecutor(Xpkeeper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
//        if (cmd.getName().equalsIgnoreCase("giveXP")) {
//            if (sender instanceof Player) {
//                Player player = (Player) sender;
//                XPKCalculator xpkc = new XPKCalculator(player);
//                int xp = xpkc.getCurrentExp();
//                int i = 0;
//                try {
//                    i = Integer.parseInt(args[0]);
//                } catch (NumberFormatException nfe) {
//                    System.err.println("[XPKeeper] could not convert to number]");
//                }
//                xpkc.changeExp(i);
//                return true;
//            }
//        }
//        if (cmd.getName().equalsIgnoreCase("setXP")) {
//            if (sender instanceof Player) {
//                Player player = (Player) sender;
//                XPKCalculator xpkc = new XPKCalculator(player);
//                int i = 0;
//                try {
//                    i = Integer.parseInt(args[0]);
//                } catch (NumberFormatException nfe) {
//                    System.err.println("[XPKeeper] could not convert to number]");
//                }
//                xpkc.setExp(i);
//                return true;
//            }
//        }
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
            if (args.length == 1) {
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
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryRemoveGet = "SELECT xpk_id FROM xpk WHERE player = '" + player + "'";
                ResultSet rsget = statement.executeQuery(queryRemoveGet);
                if (rsget.isBeforeFirst()) {
                    String queryRemovePlayer = "DELETE FROM xpk WHERE player = '" + player + "'";
                    statement.executeUpdate(queryRemovePlayer);
                }
                rsget.close();
                statement.close();
            } catch (SQLException e) {
                System.err.println("[XPKeeper] Could not get and remove player data: " + e);
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
        return false;
    }
}
