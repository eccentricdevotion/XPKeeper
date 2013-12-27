package me.eccentric_nz.plugins.xpkeeper;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class XPKPistonListener implements Listener {

    private final XPKeeper plugin;
    private final List<BlockFace> faces = new ArrayList<BlockFace>();

    public XPKPistonListener(XPKeeper plugin) {
        this.plugin = plugin;
        this.faces.add(BlockFace.EAST);
        this.faces.add(BlockFace.WEST);
        this.faces.add(BlockFace.NORTH);
        this.faces.add(BlockFace.SOUTH);
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
        if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
            return isXPKSign(b);
        } else {
            // check if there is an XPKeeper sign attached to the block
            if (b.getRelative(BlockFace.UP).getType() == Material.SIGN_POST) {
                return isXPKSign(b.getRelative(BlockFace.UP));
            }
            for (BlockFace bf : faces) {
                if (b.getRelative(bf).getType() == Material.WALL_SIGN) {
                    return isXPKSign(b.getRelative(bf));
                }
            }
            return false;
        }
    }

    private boolean isXPKSign(Block b) {
        Sign s = (Sign) b.getState();
        String line = s.getLine(0);
        return line.equalsIgnoreCase("[" + plugin.getConfig().getString("firstline") + "]");
    }
}
