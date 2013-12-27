package me.eccentric_nz.plugins.xpkeeper;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class XPKarrgghh implements Listener {

    private final XPKeeper plugin;

    public XPKarrgghh(XPKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String firstline = plugin.getConfig().getString("firstline");
        List<Block> blockList = new ArrayList<Block>();
        blockList.addAll(event.blockList());
        for (Block block : blockList) {
            if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) block.getState();
                String line0 = sign.getLine(0);
                if (line0.equalsIgnoreCase("[" + firstline + "]")) {
                    event.blockList().remove(block);
                    if (block.getType() == Material.SIGN_POST) {
                        Block blockdown = block.getRelative(BlockFace.DOWN, 1);
                        event.blockList().remove(blockdown);
                    }
                    if (block.getType() == Material.WALL_SIGN) {
                        Block blockbehind = null;
                        byte data = block.getData();
                        if (data == 4) {
                            blockbehind = block.getRelative(BlockFace.SOUTH, 1);
                        }
                        if (data == 5) {
                            blockbehind = block.getRelative(BlockFace.NORTH, 1);
                        }
                        if (data == 3) {
                            blockbehind = block.getRelative(BlockFace.EAST, 1);
                        }
                        if (data == 2) {
                            blockbehind = block.getRelative(BlockFace.WEST, 1);
                        }
                        event.blockList().remove(blockbehind);
                    }
                }
            }
        }
    }
}
