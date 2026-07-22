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
import me.eccentric_nz.xpkeeper.XPKDatabase;
import me.eccentric_nz.xpkeeper.XPKUtils;
import me.eccentric_nz.xpkeeper.XPKeeper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;

public class ForceCommandNode {

    private final XPKeeper plugin;
    private final XPKDatabase service = XPKDatabase.getInstance();

    public ForceCommandNode(XPKeeper plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("xpkforceremove")
                .requires(ctx -> ctx.getSender().hasPermission("xpkeeper.force"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    process(sender, (Player) sender);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> {
                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                            Player player = targetResolver.resolve(ctx.getSource()).getFirst();
                            CommandSender sender = ctx.getSource().getSender();
                            process(sender, player);
                            return Command.SINGLE_SUCCESS;
                        }));
        return command.build();
    }

    void process(CommandSender sender, Player player) {
        UUID uuid = player.getUniqueId();
        Statement statement = null;
        ResultSet rsget = null;
        try {
            Connection connection = service.getConnection();
            statement = connection.createStatement();
            String queryRemoveGet = "SELECT xpk_id FROM xpk WHERE uuid = '" + uuid + "'";
            rsget = statement.executeQuery(queryRemoveGet);
            if (rsget.isBeforeFirst()) {
                String queryRemovePlayer = "DELETE FROM xpk WHERE uuid = '" + uuid + "'";
                statement.executeUpdate(queryRemovePlayer);
                XPKUtils.xpkMessage(sender, "All database entries for " + player.getName() + " were removed.");
                if (player != null) {
                    plugin.trackOps.add(player.getUniqueId());
                    XPKUtils.xpkMessage(sender, plugin.getConfig().getString("messages.click_sign"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "Could not get and remove player data: " + e);
        } finally {
            try {
                if (rsget != null) {
                    rsget.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.INFO, "Could not close result set/statement when removing player data: " + e);
            }
        }
    }
}
