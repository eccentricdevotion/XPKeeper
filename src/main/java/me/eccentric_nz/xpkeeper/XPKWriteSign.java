/*
 * Copyright (C) 2023 eccentric_nz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.eccentric_nz.xpkeeper;

import io.papermc.lib.PaperLib;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;

/**
 *
 * @author eccentric_nz
 */
public class XPKWriteSign {

    private static final List<Side> sides = Arrays.asList(Side.FRONT, Side.BACK);

    /**
     * Set both sides of a sign
     *
     * @param sign the sign to update
     * @param edit the text to set on line 1
     */
    public static void update(Sign sign, String edit) {
        for (Side s : sides) {
            SignSide side = sign.getSide(s);
            side.setLine(0, edit);
            setWaxed(sign);
            sign.update();
        }
    }

    /**
     * Set both sides of a sign
     *
     * @param sign the sign to update
     * @param level the level to set on line 3
     * @param xp the xp to set on line 4
     */
    public static void update(Sign sign, int level, int xp) {
        for (Side s : sides) {
            SignSide side = sign.getSide(s);
            side.setLine(2, "Level: " + level);
            side.setLine(3, "XP: " + xp);
            setWaxed(sign);
            sign.update();
        }
    }

    /**
     * Set both sides of a sign
     *
     * @param sign the sign to update
     * @param name the player's name to set on line 2
     * @param level the level to set on line 3
     * @param xp the xp to set on line 4
     */
    public static void update(Sign sign, String name, int level, int xp) {
        for (Side s : sides) {
            SignSide side = sign.getSide(s);
            side.setLine(1, name);
            side.setLine(2, "Level: " + level);
            side.setLine(3, "XP: " + xp);
            setWaxed(sign);
            sign.update();
        }
    }

    /**
     * Set both sides of a sign
     *
     * @param sign the sign to update
     * @param keeper the identifier to set on line 1
     * @param name the player's name to set on line 2
     * @param level the level to set on line 3
     * @param xp the xp to set on line 4
     */
    public static void update(Sign sign, String keeper, String name, String level, String xp) {
        for (Side s : sides) {
            SignSide side = sign.getSide(s);
            side.setLine(0, keeper);
            side.setLine(1, name);
            side.setLine(2, level);
            side.setLine(3, xp);
            setWaxed(sign);
            sign.update();
        }
    }

    private static void setWaxed(Sign sign) {
        if (PaperLib.isPaper()) {
            Location l = sign.getLocation();
            ServerLevel world = ((CraftWorld) sign.getWorld()).getHandle();
            SignBlockEntity sbe = (SignBlockEntity) world.getBlockEntity(new BlockPos(l.getBlockX(), l.getBlockY(), l.getBlockZ()));
            sbe.setWaxed(true);
        } else {
            sign.setWaxed(true);
        }
    }
}