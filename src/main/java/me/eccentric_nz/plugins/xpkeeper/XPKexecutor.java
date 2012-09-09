/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.eccentric_nz.plugins.xpkeeper;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class XPKexecutor extends JavaPlugin implements CommandExecutor {

    private Xpkeeper plugin;

    public XPKexecutor(Xpkeeper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setXP")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int i = 0;
                try {
                    i = Integer.parseInt(args[0]);
                } catch (NumberFormatException nfe) {
                    System.err.println("[XPKeeper] could not convert to number]");
                }
                player.setLevel(i);
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("xpkremove")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.trackPlayers.put(player.getName(), true);
                player.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Click the XPKeeper sign you wish to remove.");
                return true;
            }
        }
        return false;
    }
}
