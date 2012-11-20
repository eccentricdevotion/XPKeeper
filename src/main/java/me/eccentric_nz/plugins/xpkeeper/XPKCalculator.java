package me.eccentric_nz.plugins.xpkeeper;

import org.bukkit.entity.Player;

public class XPKCalculator {

    private Xpkeeper plugin;

    public XPKCalculator(Xpkeeper plugin) {
        this.plugin = plugin;
    }

    public int calculateTotalExp(int start, double total, int end) {
        if (start == end) {
            return 0;
        }
        if (start < 16) {
            total = 17.0D;
        } else if ((start >= 16) && (start < 31)) {
            total = 17 + (start - 15) * 3;
        } else {
            total = 62 + (start - 30) * 7;
        }
        return (int) (total + calculateTotalExp(start - 1, total, end));
    }

    public int getMaxLevel(Player p) {
        double expRemaining = plugin.getKeptXP(p.getName(), p.getLocation().getWorld().getName());

        int currentLevel = p.getLevel();
        int levels = 0;
        boolean notEnough = false;
        while (!notEnough) {
            int expNeeded = calculateTotalExp(currentLevel, 0.0D, currentLevel - 1);
            if (expRemaining >= expNeeded) {
                expRemaining -= expNeeded;
                currentLevel++;
                levels++;
            } else {
                notEnough = true;
            }
        }
        return levels;
    }
}