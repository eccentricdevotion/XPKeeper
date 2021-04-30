package me.eccentric_nz.xpkeeper;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;
import java.util.List;

public class XPKBreak implements Listener {

    private final XPKeeper plugin;
    List<BlockFace> faces = Arrays.asList(BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH);

    public XPKBreak(XPKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBreakSign(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();
        if (Tag.SIGNS.isTagged(blockType)) {
            // check the text on the sign
            String firstline = plugin.getConfig().getString("firstline");
            Sign sign = (Sign) block.getState();
            String line0 = sign.getLine(0);
            String line1 = sign.getLine(1);
            String line2 = sign.getLine(2);
            String line3 = sign.getLine(3);
            if (ChatColor.stripColor(line0).equalsIgnoreCase("[" + firstline + "]")) {
                event.setCancelled(true);
                sign.setLine(0, line0);
                sign.setLine(1, line1);
                sign.setLine(2, line2);
                sign.setLine(3, line3);
                sign.update();
                player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.use_command"));
            }
        } else {
            // check if breaking block underneath or behind sign
            for (BlockFace bf : faces) {
                Block faceBlock = block.getRelative(bf);
                Material faceBlockType = faceBlock.getType();
                if (Tag.WALL_SIGNS.isTagged(faceBlockType)) {
                    BlockFace attachedFace = ((WallSign) faceBlock.getState().getBlockData()).getFacing();
                    if (bf.equals(attachedFace)) {
                        xpkSign(faceBlock, event, player);
                    }
                }
                if (bf.equals(BlockFace.UP) && Tag.STANDING_SIGNS.isTagged(faceBlockType)) {
                    xpkSign(faceBlock, event, player);
                }
            }
        }
    }

    private void xpkSign(Block b, BlockBreakEvent e, Player p) {
        String firstline = plugin.getConfig().getString("firstline");
        Sign sign = (Sign) b.getState();
        String line0 = sign.getLine(0);
        String line1 = sign.getLine(1);
        String line2 = sign.getLine(2);
        String line3 = sign.getLine(3);
        if (ChatColor.stripColor(line0).equalsIgnoreCase("[" + firstline + "]")) {
            e.setCancelled(true);
            sign.setLine(0, line0);
            sign.setLine(1, line1);
            sign.setLine(2, line2);
            sign.setLine(3, line3);
            sign.update();
            p.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.no_grief"));
        }
    }
}
