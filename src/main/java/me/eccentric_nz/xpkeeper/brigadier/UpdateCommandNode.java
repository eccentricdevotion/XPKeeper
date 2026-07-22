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
import org.bukkit.World;
import org.bukkit.entity.Player;

public class UpdateCommandNode {

    private final XPKeeper plugin;

    public UpdateCommandNode(XPKeeper plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("xpkupdate")
                .requires(ctx -> ctx.getSender() instanceof Player)
                .then(Commands.argument("world", ArgumentTypes.world())
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getSender();
                            String world = ctx.getArgument("world", World.class).getName();
                            plugin.trackUpdaters.put(player.getUniqueId(), world);
                            XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.update_sign"));
                            return Command.SINGLE_SUCCESS;
                        }));
        return command.build();
    }
}
