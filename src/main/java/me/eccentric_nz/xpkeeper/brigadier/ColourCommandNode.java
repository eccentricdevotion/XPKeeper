package me.eccentric_nz.xpkeeper.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.eccentric_nz.xpkeeper.XPKCalculator;
import me.eccentric_nz.xpkeeper.XPKUtils;
import me.eccentric_nz.xpkeeper.XPKeeper;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;

public class ColourCommandNode {

    private final XPKeeper plugin;
    public final HashMap<String, String> colours;

    public ColourCommandNode(XPKeeper plugin) {
        this.plugin = plugin;
        colours = new HashMap<>();
        colours.put("&0", "Black");
        colours.put("&1", "Dark Blue");
        colours.put("&2", "Dark Green");
        colours.put("&3", "Dark Aqua");
        colours.put("&4", "Dark Red");
        colours.put("&5", "Purple");
        colours.put("&6", "Gold");
        colours.put("&7", "Grey");
        colours.put("&8", "Dark Grey");
        colours.put("&9", "Indigo");
        colours.put("&a", "Bright Green");
        colours.put("&b", "Aqua");
        colours.put("&c", "Red");
        colours.put("&d", "Pink");
        colours.put("&e", "Yellow");
        colours.put("&f", "White");
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("xpkcolour")
                .requires(ctx -> ctx.getSender().hasPermission("xpkeeper.admin"))
                .then(Commands.argument("colour", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String c = StringArgumentType.getString(ctx, "colour").toLowerCase(Locale.ROOT);
                            if (!colours.containsKey(c)) {
                                XPKUtils.xpkMessage(ctx.getSource().getSender(), " You must specify a colour code like this: &6");
                                return Command.SINGLE_SUCCESS;
                            }
                            plugin.getConfig().set("firstline_colour", c);
                            plugin.saveConfig();
                            XPKUtils.xpkMessage(ctx.getSource().getSender(), " firstline_colour config value set to: " + colours.get(c));
                            return Command.SINGLE_SUCCESS;
                        }));
        return command.build();
    }
}
