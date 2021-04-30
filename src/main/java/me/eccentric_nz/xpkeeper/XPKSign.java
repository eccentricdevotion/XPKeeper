package me.eccentric_nz.xpkeeper;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.UUID;

public class XPKSign implements Listener {

    private final XPKeeper plugin;

    public XPKSign(XPKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String xpkLine = event.getLine(0);
        String firstLine = "[" + plugin.getConfig().getString("firstline") + "]";
        if (firstLine.equalsIgnoreCase(xpkLine)) {
            Player player = event.getPlayer();
            if (player.hasPermission("xpkeeper.use")) {
                UUID uuid = player.getUniqueId();
                String world = event.getBlock().getWorld().getName();
                String playerNameStr = player.getName();
                String sign_str = playerNameStr;
                UUID signUuid = UUID.randomUUID();
                if (playerNameStr.length() > 15) {
                    sign_str = playerNameStr.substring(0, 14);
                }
                plugin.insKeptXP(uuid, world, sign_str, signUuid.toString());
                String flc = plugin.getConfig().getString("firstline_colour");
                if (!flc.equals("&0")) {
                    event.setLine(0, ChatColor.translateAlternateColorCodes('&', flc) + firstLine);
                }
                event.setLine(1, sign_str);
                event.setLine(2, "Level: 0");
                event.setLine(3, "XP: 0");
                Sign sign = (Sign) event.getBlock().getState();
                sign.getPersistentDataContainer().set(plugin.getNskSign(), plugin.getPersistentDataTypeUUID(), signUuid);
                sign.getPersistentDataContainer().set(plugin.getNskPlayer(), plugin.getPersistentDataTypeUUID(), uuid);
                sign.update();
            } else {
                event.setLine(0, "");
                player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.no_perms_create"));
            }
        }
    }
}
