/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.xpkeeper;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author eccentric_nz
 */
public class XPKPlayer implements Listener {

    private final XPKeeper plugin;

    public XPKPlayer(XPKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Block block = event.getClickedBlock();
        if (block != null && Tag.ALL_SIGNS.getValues().contains(block.getType())) {
            String firstLine = plugin.getConfig().getString("firstline");
            Sign sign = (Sign) block.getState();
            String first = ChatColor.stripColor(sign.getLine(0));
            if (first.equalsIgnoreCase("[" + firstLine + "]")) {
                event.setCancelled(true);
                String world = block.getWorld().getName();
                String second = ChatColor.stripColor(sign.getLine(1));
                // is it the player's XPKeeper sign?
                if (plugin.isPlayersXPKSign(sign, uuid, world, second)) {
                    String signUuid = "";
                    boolean shouldUpdate = !sign.getPersistentDataContainer().has(plugin.getNskSign(), plugin.getPersistentDataTypeUUID());
                    if (!shouldUpdate) {
                        UUID su = sign.getPersistentDataContainer().get(plugin.getNskSign(), plugin.getPersistentDataTypeUUID());
                        if (su != null) {
                            signUuid = su.toString();
                        }
                    }
                    if (plugin.trackPlayers.contains(uuid)) {
                        plugin.trackPlayers.remove(uuid);
                        // set the sign block to AIR and delete the XPKeeper data
                        Material material = block.getType();
                        String check = material.toString();
                        if (check.contains("WALL")) {
                            material = Material.valueOf(check.replace("WALL_", ""));
                        }
                        block.setType(Material.AIR);
                        // drop a sign
                        Location l = block.getLocation();
                        World w = l.getWorld();
                        w.dropItemNaturally(l, new ItemStack(material, 1));
                        // return any kept XP
                        int keptXP = plugin.getKeptXP(uuid, world, signUuid);
                        new XPKCalculator(player).changeExp(keptXP);
                        // remove database record
                        plugin.delKeptXP(uuid, world, signUuid);
                        player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.removed"));
                    } else if (plugin.trackUpdaters.containsKey(uuid)) {
                        world = (plugin.trackUpdaters.get(uuid) != null) ? plugin.trackUpdaters.get(uuid) : "";
                        plugin.trackUpdaters.remove(uuid);
                        // update sign with persistent data
                        if (!sign.getPersistentDataContainer().has(plugin.getNskPlayer(), plugin.getPersistentDataTypeUUID())) {
                            sign.getPersistentDataContainer().set(plugin.getNskPlayer(), plugin.getPersistentDataTypeUUID(), uuid);
                        }
                        if (!sign.getPersistentDataContainer().has(plugin.getNskSign(), plugin.getPersistentDataTypeUUID())) {
                            sign.getPersistentDataContainer().set(plugin.getNskSign(), plugin.getPersistentDataTypeUUID(), UUID.randomUUID());
                        }
                        if (!world.isEmpty()) {
                            XPKCalculator xpkc = new XPKCalculator(player);
                            // calculate level and update the sign
                            int keptXP = plugin.getKeptXP(uuid, world, signUuid);
                            int level = xpkc.getLevelForExp(keptXP);
                            int levelXP = xpkc.getXpForLevel(level);
                            int leftoverXP = keptXP - levelXP;
                            XPKWriteSign.update(sign, level, leftoverXP, world, player.getWorld().getName(), uuid);
                            player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.updated"));
                        }
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
                            int keptXP = plugin.getKeptXP(uuid, world, signUuid);
                            // int keptLevel = plugin.getKeptLevel(uuid, world);
                            int newXPAmount = xp + keptXP;
                            int setXP = 0;
                            int newLevel = xpkc.getLevelForExp(newXPAmount);
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
                                    newXPAmount = xpkc.getXpForLevel((int) l);
                                    setXP = (xp + keptXP) - newXPAmount;
                                }
                            }
                            plugin.setKeptXP(newXPAmount, uuid, world, signUuid);
                            // calculate level and update the sign
                            int level = xpkc.getLevelForExp(newXPAmount);
                            int levelXP = xpkc.getXpForLevel(level);
                            int leftoverXP = newXPAmount - levelXP;
                            // update sign with player's current name
                            String name = (player.getName().length() > 15) ? player.getName().substring(0, 14) : player.getName();
                            XPKWriteSign.update(sign, name, level, leftoverXP);
                            // remove XP from player
                            xpkc.setExp(setXP);
                            player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + String.format(plugin.getConfig().getString("messages.deposit"), (xp - setXP), level));
                        }
                        if (action == Action.RIGHT_CLICK_BLOCK) {
                            // get withdrawal amount - 0 = all, 5 = 5 levels
                            int withdrawAmount = plugin.getConfig().getInt("withdraw");
                            int keptXP = plugin.getKeptXP(uuid, world, signUuid);
                            if (withdrawAmount == 0 || player.isSneaking()) {
                                // withdraw XP
                                xpkc.changeExp(keptXP);
                                plugin.setKeptXP(0, uuid, world, signUuid);
                                // update the sign
                                XPKWriteSign.update(sign, 0, 0);
                                player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.withdraw_all"));
                            } else {
                                int levelXP = xpkc.getXpForLevel(withdrawAmount);
                                if (keptXP >= levelXP) {
                                    // calculate remaining XP amount
                                    int remainingXP = keptXP - levelXP;
                                    plugin.setKeptXP(remainingXP, uuid, world, signUuid);
                                    int newLevel = xpkc.getLevelForExp(remainingXP);
                                    int newLevelXP = xpkc.getXpForLevel(newLevel);
                                    int leftoverXP = remainingXP - newLevelXP;
                                    XPKWriteSign.update(sign, newLevel, leftoverXP);
                                    xpkc.changeExp(levelXP);
                                    player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + String.format(plugin.getConfig().getString("messages.withdraw_some"), withdrawAmount));
                                } else {
                                    xpkc.changeExp(keptXP);
                                    plugin.setKeptXP(0, uuid, world, signUuid);
                                    // update the sign
                                    XPKWriteSign.update(sign, 0, 0);
                                    player.sendMessage(ChatColor.GRAY + "[XPKeeper] " + ChatColor.RESET + plugin.getConfig().getString("messages.withdraw_all"));
                                }
                            }
                        }
                        if (shouldUpdate) {
                            UUID su = UUID.randomUUID();
                            sign.getPersistentDataContainer().set(plugin.getNskSign(), plugin.getPersistentDataTypeUUID(), su);
                            sign.getPersistentDataContainer().set(plugin.getNskPlayer(), plugin.getPersistentDataTypeUUID(), uuid);
                            sign.update();
                            plugin.updateXPKRecord(uuid, world, su.toString());
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
