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
import org.bukkit.entity.Player;

public class GiveCommandNode {

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("xpkgive")
                .requires(ctx -> ctx.getSender().hasPermission("xpkeeper.admin"))
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                    Player player = targetResolver.resolve(ctx.getSource()).getFirst();
                                    XPKCalculator calculator = new XPKCalculator(player);
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    calculator.changeExp(amount);
                                    return Command.SINGLE_SUCCESS;
                                })));
        return command.build();
    }
}
