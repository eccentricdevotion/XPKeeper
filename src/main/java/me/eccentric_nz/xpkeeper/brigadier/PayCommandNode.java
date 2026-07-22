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

public class PayCommandNode {

    private final XPKeeper plugin;

    public PayCommandNode(XPKeeper plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("xpkpay")
                .requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().hasPermission("xpkeeper.pay"))
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    Player giver = (Player) ctx.getSource().getSender();
                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                    Player receiver = targetResolver.resolve(ctx.getSource()).getFirst();
                                    XPKCalculator xpkc_g = new XPKCalculator(giver);
                                    XPKCalculator xpkc_r = new XPKCalculator(receiver);
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    double checkEnough = xpkc_g.getCurrentExp();
                                    if (amount > checkEnough) {
                                        XPKUtils.xpkMessage(giver, plugin.getConfig().getString("messages.not_enough"));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    xpkc_r.changeExp(amount);
                                    xpkc_g.changeExp(-amount);
                                    XPKUtils.xpkMessage(giver, String.format(plugin.getConfig().getString("messages.giver"), receiver.getName(), amount));
                                    XPKUtils.xpkMessage(receiver, String.format(plugin.getConfig().getString("messages.reciever"), giver.getName(), amount));
                                    return Command.SINGLE_SUCCESS;
                                })));
        return command.build();
    }
}
