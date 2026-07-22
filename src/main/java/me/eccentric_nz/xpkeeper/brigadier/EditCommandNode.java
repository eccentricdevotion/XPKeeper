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
import me.eccentric_nz.xpkeeper.XPKWriteSign;
import me.eccentric_nz.xpkeeper.XPKeeper;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class EditCommandNode {

    private final XPKeeper plugin;

    public EditCommandNode(XPKeeper plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("xpkedit")
                .requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().hasPermission("xpkeeper.editsign"))
                .then(Commands.argument("line", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getSender();
                            Sign sign;
                            try {
                                sign = (Sign) player.getTargetBlock(null, 10).getState();
                            } catch (NullPointerException ex) {
                                XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.no_sign"));
                                return Command.SINGLE_SUCCESS;
                            } catch (ClassCastException ex) {
                                XPKUtils.xpkMessage(player, plugin.getConfig().getString("messages.look_sign"));
                                return Command.SINGLE_SUCCESS;
                            }
                            String newline = StringArgumentType.getString(ctx, "line");
                            XPKWriteSign.update(sign, newline);
                            return Command.SINGLE_SUCCESS;
                        }));
        return command.build();
    }
}
