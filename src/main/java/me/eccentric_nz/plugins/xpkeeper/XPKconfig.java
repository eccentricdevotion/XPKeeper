package me.eccentric_nz.plugins.xpkeeper;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class XPKconfig {

    private final XPKeeper plugin;
    private FileConfiguration config = null;
    private File configFile = null;
    HashMap<String, String> strOptions = new HashMap<String, String>();
    HashMap<String, Integer> intOptions = new HashMap<String, Integer>();
    HashMap<String, Boolean> boolOptions = new HashMap<String, Boolean>();

    public XPKconfig(XPKeeper plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
        boolOptions.put("must_use_fist", true);
        boolOptions.put("set_limits", false);
        intOptions.put("withdraw", 5);
        strOptions.put("firstline", "XPKeeper");
        strOptions.put("firstline_colour", "&0");
        strOptions.put("messages.arguments", "Not enough command arguments!");
        strOptions.put("messages.click_sign", "Click the XPKeeper sign you wish to remove.");
        strOptions.put("messages.deposit", "You deposited %d XP and have reached level %d :)");
        strOptions.put("messages.giver", "You payed %s %d XP :)");
        strOptions.put("messages.have_sign", "You already have an XPKeeper in this world!");
        strOptions.put("messages.look_sign", "You aren't looking at a sign!");
        strOptions.put("messages.no_grief", "Stop trying to grief this XPKeeper sign!");
        strOptions.put("messages.no_perms_command", "You do not have permission to use that command!");
        strOptions.put("messages.no_perms_create", "You do not have permission to make an XPKeeper sign!");
        strOptions.put("messages.no_player", "Could not find that player!");
        strOptions.put("messages.no_sign", "Couldn't find the sign, maybe you're too far away.");
        strOptions.put("messages.not_enough", "You don't have enough XP! Try withdrawing from your XPKeeper sign first.");
        strOptions.put("messages.not_your_sign", "Get your own sign!");
        strOptions.put("messages.reciever", "%s payed you %d XP :)");
        strOptions.put("messages.removed", "The XPKeeper was successfully removed.");
        strOptions.put("messages.use_command", "Please use the /xpkremove command to delete an XPKeeper sign!");
        strOptions.put("messages.use_fist", "You must hit the sign with your fist.");
        strOptions.put("messages.withdraw_all", "You withdrew all your XP!");
        strOptions.put("messages.withdraw_some", "You withdrew %d XP Levels!");
    }

    public void checkConfig() {
        int i = 0;
        // boolean values
        for (Map.Entry<String, Boolean> entry : boolOptions.entrySet()) {
            if (!config.contains(entry.getKey())) {
                plugin.getConfig().set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        // int values
        for (Map.Entry<String, Integer> entry : intOptions.entrySet()) {
            if (!config.contains(entry.getKey())) {
                plugin.getConfig().set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        // string values
        for (Map.Entry<String, String> entry : strOptions.entrySet()) {
            if (!config.contains(entry.getKey())) {
                plugin.getConfig().set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        if (!config.contains("limits")) {
            List<Integer> limits = Arrays.asList(new Integer[]{30, 50, 100});
            plugin.getConfig().set("limits", limits);
            i++;
        }
        plugin.saveConfig();
        if (i > 0) {
            System.out.println("[XPKeeper] Added " + i + " new items to config");
        }
    }
}
