package me.eccentric_nz.plugins.xpkeeper;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class XPKplayer implements Listener {

    private final XPKeeper plugin;

    public XPKplayer(XPKeeper plugin) {
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
            Action action = event.getAction();
            if (blockType == Material.WALL_SIGN || blockType == Material.SIGN_POST) {
                // check the text on the sign
                String firstline = plugin.getConfig().getString("firstline");
                Sign sign = (Sign) block.getState();
                String line0 = sign.getLine(0);
                String line1 = sign.getLine(1);
                // check name length
                String sign_str = playerNameStr;
                if (playerNameStr.length() > 15) {
                    sign_str = playerNameStr.substring(0, 15);
                }
                if (line0.equalsIgnoreCase("[" + firstline + "]")) {
                    if (plugin.trackPlayers.containsKey(playerNameStr) && line1.equals(sign_str)) {
                        plugin.trackPlayers.remove(playerNameStr);
                        // set the sign block to AIR and delete the XPKeeper data
                        block.setType(Material.AIR);
                        // drop a sign
                        Location l = block.getLocation();
                        World w = l.getWorld();
                        w.dropItemNaturally(l, new ItemStack(323, 1));
                        // return any kept XP
                        int keptXP = plugin.getKeptXP(playerNameStr, world);
                        new XPKCalculator(player).changeExp(keptXP);
                        // remove database record
                        plugin.delKeptXP(playerNameStr, world);
                        player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.removed"));
                    } else {
                        if (plugin.getConfig().getBoolean("must_use_fist") && inhand != Material.AIR) {
                            player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.use_fist"));
                        } else {
                            if (line1.equals(sign_str)) {
                                XPKCalculator xpkc = new XPKCalculator(player);
                                // get players XP
                                int xp = xpkc.getCurrentExp();
                                if (action == Action.LEFT_CLICK_BLOCK) {
                                    // deposit XP
                                    if (line1.equals(sign_str)) {
                                        // sign is set up so update the amount kept
                                        int keptXP = plugin.getKeptXP(playerNameStr, world);
                                        //int keptLevel = plugin.getKeptLevel(playerNameStr, world);
                                        int newXPamount = xp + keptXP;
                                        int setxp = 0;
                                        int newLevel = xpkc.getLevelForExp(newXPamount);
                                        if (plugin.getConfig().getBoolean("set_limits")) {
                                            List<Double> limits = plugin.getConfig().getDoubleList("limits");
                                            double l = 0;
                                            for (Double d : limits) {
                                                if (!player.hasPermission("xpkeeper.limit.bypass") && player.hasPermission("xpkeeper.limit." + d)) {
                                                    l = d;
                                                    break;
                                                }
                                            }
                                            if (l != 0 && (newLevel + 1) > (int) l) {
                                                player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + "That amount would take you over your maximum deposit level, depositing as much as we can.");
                                                newXPamount = xpkc.getXpForLevel((int) l);
                                                setxp = (xp + keptXP) - newXPamount;
                                            }
                                        }
                                        plugin.setKeptXP(newXPamount, playerNameStr, world);
                                        // calculate level and update the sign
                                        int level = xpkc.getLevelForExp(newXPamount);
                                        int levelxp = xpkc.getXpForLevel(level);
                                        int leftoverxp = newXPamount - levelxp;
                                        sign.setLine(2, "Level: " + level);
                                        sign.setLine(3, "XP: " + leftoverxp);
                                        sign.update();
                                        // remove XP from player
                                        xpkc.setExp(setxp);
                                        player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + String.format(plugin.getConfig().getString("messages.deposit"), (xp - setxp), level));
                                    }
                                }
                                if (action == Action.RIGHT_CLICK_BLOCK) {
                                    // get withdrawal amount - 0 = all, 5 = 5 levels
                                    int withdrawAmount = plugin.getConfig().getInt("withdraw");
                                    int keptXP = plugin.getKeptXP(playerNameStr, world);
                                    if (withdrawAmount == 0 || player.isSneaking()) {
                                        // withdraw XP
                                        xpkc.changeExp(keptXP);
                                        plugin.setKeptXP(0, playerNameStr, world);
                                        // update the sign
                                        sign.setLine(2, "Level: 0");
                                        sign.setLine(3, "XP: 0");
                                        player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.withdraw_all"));
                                    } else {
                                        int levelXP = xpkc.getXpForLevel(withdrawAmount);
                                        if (keptXP >= levelXP) {
                                            // calculate remaining XP amount
                                            int remainingXP = keptXP - levelXP;
                                            plugin.setKeptXP(remainingXP, playerNameStr, world);
                                            int newLevel = xpkc.getLevelForExp(remainingXP);
                                            int newlevelxp = xpkc.getXpForLevel(newLevel);
                                            int leftoverxp = remainingXP - newlevelxp;
                                            sign.setLine(2, "Level: " + newLevel);
                                            sign.setLine(3, "XP: " + leftoverxp);
                                            xpkc.changeExp(levelXP);
                                            player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + String.format(plugin.getConfig().getString("messages.withdraw_some"), withdrawAmount));
                                        } else {
                                            xpkc.changeExp(keptXP);
                                            plugin.setKeptXP(0, playerNameStr, world);
                                            // update the sign
                                            sign.setLine(2, "Level: 0");
                                            sign.setLine(3, "XP: 0");
                                            player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.withdraw_all"));
                                        }
                                    }
                                    sign.update();
                                }
                            } else {
                                player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.not_your_sign"));
                            }
                        }
                    }
                }
            }
        }
    }
}
