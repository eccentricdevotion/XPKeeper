package me.eccentric_nz.xpkeeper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.UUID;

public class XPKsign implements Listener {

    private final XPKeeper plugin;

    public XPKsign(XPKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String world = event.getBlock().getWorld().getName();
        String playerNameStr = player.getName();
        String sign_str = playerNameStr;
        if (playerNameStr.length() > 15) {
            sign_str = playerNameStr.substring(0, 14);
        }
        String xpkLine = event.getLine(0);
        String firstline = "[" + plugin.getConfig().getString("firstline") + "]";
        if (firstline.equalsIgnoreCase(xpkLine)) {
            if (player.hasPermission("xpkeeper.use")) {
                // check to see if they have a keeper already
                int keptXP = plugin.getKeptXP(uuid, world);
                if (keptXP < 0) {
                    plugin.insKeptXP(uuid, world, sign_str);
                    String flc = plugin.getConfig().getString("firstline_colour");
                    if (!flc.equals("&0")) {
                        event.setLine(0, ChatColor.translateAlternateColorCodes('&', flc) + firstline);
                    }
                    event.setLine(1, sign_str);
                    event.setLine(2, "Level: 0");
                    event.setLine(3, "XP: 0");
                } else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.have_sign"));
                }
            } else {
                event.setLine(0, "");
                player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.no_perms_create"));
            }
        }
    }
}
