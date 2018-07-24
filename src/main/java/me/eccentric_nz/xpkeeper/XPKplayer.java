/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.xpkeeper;

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

import java.util.List;
import java.util.UUID;

/**
 * @author eccentric_nz
 */
public class XPKplayer implements Listener {

    private final XPKeeper plugin;

    public XPKplayer(XPKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Block block = event.getClickedBlock();
        if (block != null && (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN)) {
            String firstline = plugin.getConfig().getString("firstline");
            Sign sign = (Sign) block.getState();
            String first = ChatColor.stripColor(sign.getLine(0));
            if (first.equalsIgnoreCase("[" + firstline + "]") || (plugin.is1_8() && first.equalsIgnoreCase(firstline))) {
                String world = block.getWorld().getName();
                String second = ChatColor.stripColor(sign.getLine(1));
                // is it the player's XPKeeper sign?
                if (plugin.isPlayersXPKSign(uuid, world, second)) {
                    if (plugin.trackPlayers.contains(uuid)) {
                        plugin.trackPlayers.remove(uuid);
                        // set the sign block to AIR and delete the XPKeeper data
                        block.setType(Material.AIR);
                        // drop a sign
                        Location l = block.getLocation();
                        World w = l.getWorld();
                        w.dropItemNaturally(l, new ItemStack(Material.SIGN, 1));
                        // return any kept XP
                        int keptXP = plugin.getKeptXP(uuid, world);
                        new XPKCalculator(player).changeExp(keptXP);
                        // remove database record
                        plugin.delKeptXP(uuid, world);
                        player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.removed"));
                    } else if (plugin.getConfig().getBoolean("must_use_fist") && !player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                        player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.use_fist"));
                    } else {
                        Action action = event.getAction();
                        XPKCalculator xpkc = new XPKCalculator(player);
                        // get players XP
                        int xp = xpkc.getCurrentExp();
                        if (action == Action.LEFT_CLICK_BLOCK) {
                            // deposit XP
                            // sign is set up so update the amount kept
                            int keptXP = plugin.getKeptXP(uuid, world);
                            //int keptLevel = plugin.getKeptLevel(uuid, world);
                            int newXPamount = xp + keptXP;
                            int setxp = 0;
                            int newLevel = xpkc.getLevelForExp(newXPamount);
                            if (plugin.getConfig().getBoolean("set_limits") && !player.hasPermission("xpkeeper.limit.bypass")) {
                                List<Double> limits = plugin.getConfig().getDoubleList("limits");
                                double l = 0;
                                for (double d : limits) {
                                    String perm = "xpkeeper.limit." + ((int) d);
                                    if (player.hasPermission(perm)) {
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
                            plugin.setKeptXP(newXPamount, uuid, world);
                            // calculate level and update the sign
                            int level = xpkc.getLevelForExp(newXPamount);
                            int levelxp = xpkc.getXpForLevel(level);
                            int leftoverxp = newXPamount - levelxp;
                            // update sign with player's current name
                            String name = (player.getName().length() > 15) ? player.getName().substring(0, 14) : player.getName();
                            sign.setLine(1, name);
                            sign.setLine(2, "Level: " + level);
                            sign.setLine(3, "XP: " + leftoverxp);
                            sign.update();
                            // remove XP from player
                            xpkc.setExp(setxp);
                            player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + String.format(plugin.getConfig().getString("messages.deposit"), (xp - setxp), level));
                        }
                        if (action == Action.RIGHT_CLICK_BLOCK) {
                            // get withdrawal amount - 0 = all, 5 = 5 levels
                            int withdrawAmount = plugin.getConfig().getInt("withdraw");
                            int keptXP = plugin.getKeptXP(uuid, world);
                            if (withdrawAmount == 0 || player.isSneaking()) {
                                // withdraw XP
                                xpkc.changeExp(keptXP);
                                plugin.setKeptXP(0, uuid, world);
                                // update the sign
                                sign.setLine(2, "Level: 0");
                                sign.setLine(3, "XP: 0");
                                sign.update();
                                player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.withdraw_all"));
                            } else {
                                int levelXP = xpkc.getXpForLevel(withdrawAmount);
                                if (keptXP >= levelXP) {
                                    // calculate remaining XP amount
                                    int remainingXP = keptXP - levelXP;
                                    plugin.setKeptXP(remainingXP, uuid, world);
                                    int newLevel = xpkc.getLevelForExp(remainingXP);
                                    int newlevelxp = xpkc.getXpForLevel(newLevel);
                                    int leftoverxp = remainingXP - newlevelxp;
                                    sign.setLine(2, "Level: " + newLevel);
                                    sign.setLine(3, "XP: " + leftoverxp);
                                    sign.update();
                                    xpkc.changeExp(levelXP);
                                    player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + String.format(plugin.getConfig().getString("messages.withdraw_some"), withdrawAmount));
                                } else {
                                    xpkc.changeExp(keptXP);
                                    plugin.setKeptXP(0, uuid, world);
                                    // update the sign
                                    sign.setLine(2, "Level: 0");
                                    sign.setLine(3, "XP: 0");
                                    sign.update();
                                    player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.withdraw_all"));
                                }
                            }
                        }
                    }
                } else if (plugin.trackOps.contains(uuid)) {
                    plugin.trackOps.remove(uuid);
                    // set the sign block to AIR
                    block.setType(Material.AIR);
                    player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.removed"));
                } else {
                    player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.not_your_sign"));
                }
            }
        }
    }
}
