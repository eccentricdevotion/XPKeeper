package me.eccentric_nz.plugins.xpkeeper;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class XPKplayer implements Listener {

    private Xpkeeper plugin;

    public XPKplayer(Xpkeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSignClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack is = player.getItemInHand();
        Material inhand = is.getType();
        String playerNameStr = player.getName();
        Block block = event.getClickedBlock();
        if (block != null) {
            String world = block.getLocation().getWorld().getName();
            Material blockType = block.getType();
            byte data = block.getData();
            Action action = event.getAction();
            if ((blockType == Material.WALL_SIGN || blockType == Material.SIGN_POST) && inhand == Material.AIR) {
                // check the text on the sign
                String firstline = plugin.getConfig().getString("firstline");
                Sign sign = (Sign) block.getState();
                String line0 = sign.getLine(0);
                String line1 = sign.getLine(1);
                int level = player.getLevel();
                int newLevel = 0;
                int tenth = 0;
                double one = 1.0;
                double XPamount = player.getExp();
                float newXPamount = 0;
                if (line0.equals("[" + firstline + "]")) {
                    if (plugin.trackPlayers.containsKey(playerNameStr) == true && line1.equals(playerNameStr)) {
                        plugin.trackPlayers.remove(playerNameStr);
                        // set the sign block to AIR and delete the XPKeeper data
                        block.setType(Material.AIR);
                        plugin.delKeptXP(playerNameStr, world);
                        player.sendMessage(ChatColor.GRAY + "[XPKeeper]" + ChatColor.RESET + " The XPKeeper was successfully removed.");
                    } else {
                        if (action == Action.LEFT_CLICK_BLOCK) {
                            // get players XP
                            // deposit XP
                            if (line1.equals(playerNameStr)) {
                                // sign is set up so update the amount kept
                                double keptXP = plugin.getKeptXP(playerNameStr, world);
                                int keptLevel = plugin.getKeptLevel(playerNameStr, world);
                                double tmpXP = XPamount + keptXP;
                                if (tmpXP > one) {
                                    newXPamount = ((Double) (tmpXP - one)).floatValue();
                                    tenth = 1;
                                } else {
                                    newXPamount = ((Double) tmpXP).floatValue();
                                }
                                newLevel = level + keptLevel + tenth;
                                plugin.setKeptXP(newLevel, newXPamount, playerNameStr, world);
                            }
                            // update the sign
                            sign.setLine(2, "Level: " + newLevel);
                            sign.setLine(3, "XP: " + newXPamount);
                            sign.update();
                            // remove XP from player
                            player.setLevel(0);
                            player.setExp(0);
                        }
                        if (action == Action.RIGHT_CLICK_BLOCK) {
                            // withdraw XP
                            int keptLevel = plugin.getKeptLevel(playerNameStr, world);
                            double keptXP = plugin.getKeptXP(playerNameStr, world);
                            double tmpXP = XPamount + keptXP;
                            if (tmpXP > one) {
                                newXPamount = ((Double) (tmpXP - one)).floatValue();
                                tenth = 1;
                            } else {
                                newXPamount = ((Double) tmpXP).floatValue();
                            }
                            newLevel = level + keptLevel + tenth;
                            player.setLevel(newLevel);
                            player.setExp(newXPamount);
                            plugin.setKeptXP(0, 0, playerNameStr, world);
                            // update the sign
                            sign.setLine(2, "Level: 0");
                            sign.setLine(3, "XP: 0");
                            sign.update();
                        }
                    }
                }
            }
        }
    }
}
