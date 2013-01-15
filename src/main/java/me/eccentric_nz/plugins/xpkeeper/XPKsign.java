package me.eccentric_nz.plugins.xpkeeper;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class XPKsign implements Listener {

    private XPKeeper plugin;

    public XPKsign(XPKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String world = event.getBlock().getWorld().getName();
        String playerNameStr = player.getName();
        String xpkLine = event.getLine(0);
        String firstline = "[" + plugin.getConfig().getString("firstline") + "]";
        if (firstline.equalsIgnoreCase(xpkLine)) {
            if (player.hasPermission("xpkeeper.use")) {
                // check to see if they have a keeper already
                int keptXP = plugin.getKeptXP(playerNameStr, world);
                if (keptXP < 0) {
                    plugin.insKeptXP(playerNameStr, world);
                    event.setLine(1, playerNameStr);
                    event.setLine(2, "Level: 0");
                    event.setLine(3, "XP: 0");
                } else {
                    event.setCancelled(true);
                    player.sendMessage("You already have an XPKeeper in this world!");
                }
            } else {
                event.setLine(0, "");
                player.sendMessage("You do not have permission to make an XPKeeper sign!");
            }
        }
    }
}