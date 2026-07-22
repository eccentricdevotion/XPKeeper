package me.eccentric_nz.xpkeeper.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

public class FistCommandNode {

    private final XPKeeper plugin;

    public FistCommandNode(XPKeeper plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("xpkfist")
                .requires(ctx -> ctx.getSender() instanceof Player player && player.hasPermission("xpkeeper.fist"))
                .executes(ctx -> {
                    boolean bool = plugin.getConfig().getBoolean("must_use_fist");
                    plugin.getConfig().set("must_use_fist", !bool);
                    plugin.saveConfig();
                    XPKUtils.xpkMessage(ctx.getSource().getSender(), "must_use_fist config value set to: " + !bool);
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
