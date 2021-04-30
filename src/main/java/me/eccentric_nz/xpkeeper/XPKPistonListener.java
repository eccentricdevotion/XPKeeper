package me.eccentric_nz.xpkeeper;

import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.ArrayList;
import java.util.List;

public class XPKPistonListener implements Listener {

    private final XPKeeper plugin;
    private final List<BlockFace> faces = new ArrayList<>();

    public XPKPistonListener(XPKeeper plugin) {
        this.plugin = plugin;
        faces.add(BlockFace.EAST);
        faces.add(BlockFace.WEST);
        faces.add(BlockFace.NORTH);
        faces.add(BlockFace.SOUTH);
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (hasXPKSign(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isSticky() && checkXPKSign(event.getRetractLocation().getBlock())) {
            event.setCancelled(true);
        }
    }

    public boolean hasXPKSign(List<Block> blocks) {
        for (Block b : blocks) {
            if (checkXPKSign(b)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkXPKSign(Block b) {
        if (Tag.SIGNS.isTagged(b.getType())) {
            return isXPKSign(b);
        } else {
            // check if there is an XPKeeper sign attached to the block
            if (Tag.STANDING_SIGNS.isTagged(b.getRelative(BlockFace.UP).getType())) {
                return isXPKSign(b.getRelative(BlockFace.UP));
            }
            for (BlockFace bf : faces) {
                if (Tag.WALL_SIGNS.isTagged(b.getRelative(bf).getType())) {
                    return isXPKSign(b.getRelative(bf));
                }
            }
            return false;
        }
    }

    private boolean isXPKSign(Block b) {
        Sign s = (Sign) b.getState();
        String line = ChatColor.stripColor(s.getLine(0));
        return line.equalsIgnoreCase("[" + plugin.getConfig().getString("firstline") + "]");
    }
}
