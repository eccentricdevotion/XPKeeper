package me.eccentric_nz.xpkeeper;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

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
                if (Tag.SIGNS.isTagged(event.getBlock().getType()) && playerNameStr.length() > 15) {
                    sign_str = playerNameStr.substring(0, 14);
                }
                if (Tag.ALL_HANGING_SIGNS.isTagged(event.getBlock().getType()) && playerNameStr.length() > 11) {
                    sign_str = playerNameStr.substring(0, 10);
                }
                plugin.insKeptXP(uuid, world, sign_str, signUuid.toString());
                String flc = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("firstline_colour")) + firstLine;
                Sign sign = (Sign) event.getBlock().getState();
                sign.getPersistentDataContainer().set(plugin.getNskSign(), plugin.getPersistentDataTypeUUID(), signUuid);
                sign.getPersistentDataContainer().set(plugin.getNskPlayer(), plugin.getPersistentDataTypeUUID(), uuid);
                XPKWriteSign.update(sign, flc, sign_str, "Level: 0", "XP: 0");
            } else {
                event.setLine(0, "");
                player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.no_perms_create"));
            }
        }
    }
}
