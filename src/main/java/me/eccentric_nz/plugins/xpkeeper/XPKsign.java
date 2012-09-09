package me.eccentric_nz.plugins.xpkeeper;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class XPKsign implements Listener {

    private Xpkeeper plugin;

    public XPKsign(Xpkeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String world = event.getBlock().getWorld().getName();
        String playerNameStr = player.getName();
        String xpkLine = event.getLine(0);
        String firstline = plugin.getConfig().getString("firstline");
        if (xpkLine.equals("[" + firstline + "]")) {
            // check to see if they have a keeper already
            int keptLevel = plugin.getKeptLevel(playerNameStr, world);
            double keptXP = plugin.getKeptXP(playerNameStr, world);
            if (keptLevel < 0) {
                plugin.insKeptXP(playerNameStr, world);
                event.setLine(1, playerNameStr);
                event.setLine(2, "Level: 0");
                event.setLine(3, "XP: 0");
            } else {
                event.setCancelled(true);
                player.sendMessage("You already have an XPKeeper in this world!");
            }
        }
    }
}
