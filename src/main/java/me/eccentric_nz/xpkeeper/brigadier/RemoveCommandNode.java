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

public class RemoveCommandNode {

    private final XPKeeper plugin;

    public RemoveCommandNode(XPKeeper plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("xpkremove")
                .requires(ctx -> ctx.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    plugin.trackPlayers.add(player.getUniqueId());
                    XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.click_sign"));
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
