package me.eccentric_nz.plugins.xpkeeper;

import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class XPKbreak implements Listener {

    private Xpkeeper plugin;

    public XPKbreak(Xpkeeper plugin) {
        this.plugin = plugin;
    }
    List<BlockFace> faces = Arrays.asList(BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH);

    @EventHandler
    public void onPlayerBreakSign(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();
        if (blockType == Material.WALL_SIGN || blockType == Material.SIGN_POST) {
            // check the text on the sign
            String firstline = plugin.getConfig().getString("firstline");
            Sign sign = (Sign) block.getState();
            String line0 = sign.getLine(0);
            String line1 = sign.getLine(1);
            String line2 = sign.getLine(2);
            String line3 = sign.getLine(3);
            if (line0.equals("[" + firstline + "]")) {
                event.setCancelled(true);
                sign.setLine(0, line0);
                sign.setLine(1, line1);
                sign.setLine(2, line2);
                sign.setLine(3, line3);
                sign.update();
                player.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Please use the " + ChatColor.GREEN + "/xpkremove" + ChatColor.RESET + " command to delete an XPKeeper sign!");
            }
        } else {
            // check if breaking block underneath or behind sign
            for (BlockFace bf : faces) {
                Block faceBlock = block.getRelative(bf);
                Material faceBlockType = faceBlock.getType();
                if (faceBlockType == Material.WALL_SIGN || faceBlockType == Material.SIGN_POST) {
                    String firstline = plugin.getConfig().getString("firstline");
                    Sign sign = (Sign) faceBlock.getState();
                    String line0 = sign.getLine(0);
                    String line1 = sign.getLine(1);
                    String line2 = sign.getLine(2);
                    String line3 = sign.getLine(3);
                    if (line0.equals("[" + firstline + "]")) {
                        event.setCancelled(true);
                        sign.setLine(0, line0);
                        sign.setLine(1, line1);
                        sign.setLine(2, line2);
                        sign.setLine(3, line3);
                        sign.update();
                        player.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " Stop trying to grief this XPKeeper sign!");
                    }
                }
            }
        }
    }
}
