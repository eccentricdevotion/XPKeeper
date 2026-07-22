package me.eccentric_nz.xpkeeper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class XPKUtils {

    public static String stripColour(Component component) {
        return component == null ? "" : ((TextComponent) component).content();
    }

    public static void xpkMessage(Player player, String text) {
player.sendMessage(Component.text("[XPKeeper] ", NamedTextColor.GRAY).append(Component.text(text, NamedTextColor.WHITE)));
    }

    public static void xpkMessage(CommandSender sender, String text) {
        sender.sendMessage(Component.text("[XPKeeper] ", NamedTextColor.GRAY).append(Component.text(text, NamedTextColor.WHITE)));
    }
}
