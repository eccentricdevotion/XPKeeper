package me.eccentric_nz.xpkeeper;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import me.eccentric_nz.xpkeeper.brigadier.*;

import java.util.List;

public class XPKCommandRegister {

    private final ReloadableRegistrarEvent<Commands> commands;
    private final XPKeeper plugin;

    public XPKCommandRegister(ReloadableRegistrarEvent<Commands> commands, XPKeeper plugin) {
        this.commands = commands;
        this.plugin = plugin;
    }

    public void addAll() {
        commands.registrar().register(new GiveCommandNode().build(), List.of("xpkgive", "xpkg"));
        commands.registrar().register(new SetCommandNode().build(), List.of("xpkset", "xpks"));
        commands.registrar().register(new RemoveCommandNode(plugin).build(), List.of("xpkremove", "xpkr", "xpkrem"));
        commands.registrar().register(new UpdateCommandNode(plugin).build(), List.of("xpkupdate", "xpku"));
        commands.registrar().register(new ForceCommandNode(plugin).build(), List.of("xpkforceremove", "xpkfr", "xpkforce"));
        commands.registrar().register(new EditCommandNode(plugin).build(), List.of("xpkedit", "xpke"));
        commands.registrar().register(new PayCommandNode(plugin).build(), List.of("xpkpay", "xpkp"));
        // config
        commands.registrar().register(new ColourCommandNode(plugin).build(), List.of("xpkcolour", "xpkc", "xpkcolor"));
        commands.registrar().register(new FistCommandNode(plugin).build(), List.of("xpkfist", "xpkf"));
        commands.registrar().register(new LimitCommandNode(plugin).build(), List.of("xpklimit", "xpkl"));
        commands.registrar().register(new ReloadCommandNode(plugin).build(), List.of("xpkreload", "xpkrl"));
        commands.registrar().register(new WithdrawCommandNode(plugin).build(), List.of("xpkwithdraw", "xpkw"));
    }

}
