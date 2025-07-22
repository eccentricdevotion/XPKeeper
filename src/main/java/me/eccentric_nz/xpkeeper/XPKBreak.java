package me.eccentric_nz.xpkeeper;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
            if (plugin.getConfig().getBoolean("allow_raids")) {
                raidSign(block, event, player);
            } else {
                // check the text on the sign
                xpkSign(block, event, player, "messages.use_command");
            }
        } else {
            // check if breaking block underneath or behind sign
            for (BlockFace bf : faces) {
                Block faceBlock = block.getRelative(bf);
                Material faceBlockType = faceBlock.getType();
                if (Tag.WALL_SIGNS.isTagged(faceBlockType)) {
                    BlockFace attachedFace = ((WallSign) faceBlock.getState().getBlockData()).getFacing();
                    if (bf.equals(attachedFace)) {
                        xpkSign(faceBlock, event, player, "messages.no_grief");
                    }
                }
                if (bf.equals(BlockFace.UP) && Tag.STANDING_SIGNS.isTagged(faceBlockType)) {
                    xpkSign(faceBlock, event, player, "messages.no_grief");
                }
            }
        }
    }

    private void xpkSign(Block b, BlockBreakEvent e, Player p, String message) {
        String firstLine = plugin.getConfig().getString("firstline");
        Sign sign = (Sign) b.getState();
        String line0 = sign.getLine(0);
        String line1 = sign.getLine(1);
        String line2 = sign.getLine(2);
        String line3 = sign.getLine(3);
        if (ChatColor.stripColor(line0).equalsIgnoreCase("[" + firstLine + "]")) {
            e.setCancelled(true);
            XPKWriteSign.update(sign, line0, line1, line2, line3);
            p.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString(message));
        }
    }

    private void raidSign(Block b, BlockBreakEvent e, Player p) {
        String firstLine = plugin.getConfig().getString("firstline");
        Sign sign = (Sign) b.getState();
        String line0 = sign.getLine(0);
        String line1 = sign.getLine(1);
        String line2 = sign.getLine(2);
        String line3 = sign.getLine(3);
        if (ChatColor.stripColor(line0).equalsIgnoreCase("[" + firstLine + "]")) {
            // get experience from sign
            String signUuid = "";
            boolean hasUUID = sign.getPersistentDataContainer().has(plugin.getNskSign(), plugin.getPersistentDataTypeUUID());
            if (hasUUID) {
                UUID su = sign.getPersistentDataContainer().get(plugin.getNskSign(), plugin.getPersistentDataTypeUUID());
                if (su != null) {
                    signUuid = su.toString();
                }
            }
            Player raided = plugin.getServer().getPlayer(line1);
            if (raided != null) {
                UUID uuid = raided.getUniqueId();
                World world = sign.getWorld();
                // drop any kept XP
                int keptXP = plugin.getKeptXP(uuid, world.getName(), signUuid);
                world.spawn(sign.getLocation(), ExperienceOrb.class).setExperience(keptXP);
                // remove database record
                plugin.delKeptXP(uuid, world.getName(), signUuid);
                p.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.raided"));
            } else {
                e.setCancelled(true);
                XPKWriteSign.update(sign, line0, line1, line2, line3);
                p.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.removed"));
            }
        }
    }
}
