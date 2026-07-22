package me.eccentric_nz.xpkeeper.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.eccentric_nz.xpkeeper.XPKUtils;
import me.eccentric_nz.xpkeeper.XPKeeper;
import org.bukkit.entity.Player;

public class LimitCommandNode {

    private final XPKeeper plugin;

    public LimitCommandNode(XPKeeper plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("xpklimit")
                .requires(ctx -> ctx.getSender() instanceof Player player && player.hasPermission("xpkeeper.limit"))
                .executes(ctx -> {
                    boolean bool = plugin.getConfig().getBoolean("set_limits");
                    plugin.getConfig().set("set_limits", !bool);
                    plugin.saveConfig();
                    XPKUtils.xpkMessage(ctx.getSource().getSender(), "set_limits config value set to: " + !bool);
                    return Command.SINGLE_SUCCESS;
                });
        return command.build();
    }
}
