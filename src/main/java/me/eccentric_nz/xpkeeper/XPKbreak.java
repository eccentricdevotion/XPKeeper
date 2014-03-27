package me.eccentric_nz.xpkeeper;

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
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

public class XPKbreak implements Listener {

    private final XPKeeper plugin;

    public XPKbreak(XPKeeper plugin) {
        this.plugin = plugin;
    }
    List<BlockFace> faces = Arrays.asList(BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH);

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
            if (plugin.stripColourCode(line0).equalsIgnoreCase("[" + firstline + "]")) {
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
                if (faceBlockType == Material.WALL_SIGN) {
                    Sign sign = (Sign) faceBlock.getState();
                    MaterialData m = sign.getData();
                    BlockFace attachedFace;
                    BlockFace chkFace = null;
                    if (m instanceof Attachable) {
                        attachedFace = ((Attachable) m).getAttachedFace();
                        // get opposite face
                        switch (attachedFace) {
                            case EAST:
                                chkFace = BlockFace.WEST;
                                break;
                            case NORTH:
                                chkFace = BlockFace.SOUTH;
                                break;
                            case WEST:
                                chkFace = BlockFace.EAST;
                                break;
                            default:
                                chkFace = BlockFace.NORTH;
                                break;
                        }
                    }
                    if (bf.equals(chkFace)) {
                        xpkSign(faceBlock, event, player);
                    }
                }
                if (bf.equals(BlockFace.UP) && faceBlockType == Material.SIGN_POST) {
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
        if (plugin.stripColourCode(line0).equalsIgnoreCase("[" + firstline + "]")) {
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
