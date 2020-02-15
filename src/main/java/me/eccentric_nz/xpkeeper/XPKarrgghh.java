package me.eccentric_nz.xpkeeper;

import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

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
        List<Block> blockList = new ArrayList<>();
        blockList.addAll(event.blockList());
        blockList.forEach((block) -> {
            if (Tag.SIGNS.isTagged(block.getType())) {
                Sign sign = (Sign) block.getState();
                String line0 = plugin.stripColourCode(sign.getLine(0));
                if (line0.equalsIgnoreCase("[" + firstline + "]")) {
                    event.blockList().remove(block);
                    if (Tag.STANDING_SIGNS.isTagged(block.getType())) {
                        Block blockdown = block.getRelative(BlockFace.DOWN, 1);
                        event.blockList().remove(blockdown);
                    }
                    if (Tag.WALL_SIGNS.isTagged(block.getType())) {
                        WallSign data = (WallSign) block.getBlockData();
                        Block blockbehind = block.getRelative(data.getFacing().getOppositeFace(), 1);
                        event.blockList().remove(blockbehind);
                    }
                }
            }
        });
    }
}
