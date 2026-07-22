package me.eccentric_nz.xpkeeper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Tag;
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
        String xpkLine = XPKUtils.stripColour(event.line(0));
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
                Component flc = LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getConfig().getString("firstline_colour") + firstLine);
                Sign sign = (Sign) event.getBlock().getState();
                sign.getPersistentDataContainer().set(plugin.getNskSign(), plugin.getPersistentDataTypeUUID(), signUuid);
                sign.getPersistentDataContainer().set(plugin.getNskPlayer(), plugin.getPersistentDataTypeUUID(), uuid);
                final String name = sign_str;
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () ->
                        XPKWriteSign.update(sign, flc, Component.text(name), Component.text("Level: 0"), Component.text("XP: 0")), 2L);
            } else {
                event.line(0, Component.text(""));
                XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.no_perms_create"));
            }
        }
    }
}
