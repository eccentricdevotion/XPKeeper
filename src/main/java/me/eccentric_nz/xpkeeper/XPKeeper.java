package me.eccentric_nz.xpkeeper;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Sign;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class XPKeeper extends JavaPlugin {

    XPKdatabase service;
    private XPKexecutor xpkExecutor;
    XPKsign signListener;
    XPKplayer playerListener;
    XPKbreak breakListener;
    XPKarrgghh explodeListener;
    XPKPistonListener pistonListener;
    PluginManager pm;
    public List<String> trackPlayers;

    @Override
    public void onDisable() {
        closeDatabase();
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        XPKconfig xpkc = new XPKconfig(this);
        xpkc.checkConfig();
        service = XPKdatabase.getInstance();
        try {
            String path = getDataFolder() + File.separator + "XPKeeper.db";
            service.setConnection(path);
            service.createTable();
        } catch (Exception e) {
            System.err.println("[XPKeeper] Connection and Tables Error: " + e);
        }
        pm = getServer().getPluginManager();
        // update database add and populate uuid fields
        if (!getConfig().getBoolean("uuid_conversion_done")) {
            XPKeeperUUIDConverter uc = new XPKeeperUUIDConverter(this);
            if (!uc.convert()) {
                // conversion failed
                System.err.println("[XPKeeper]" + ChatColor.RED + "UUID conversion failed, disabling...");
                pm.disablePlugin(this);
                return;
            } else {
                getConfig().set("uuid_conversion_done", true);
                saveConfig();
                System.out.println("[XPKeeper] UUID conversion successful :)");
            }
        }
        signListener = new XPKsign(this);
        playerListener = new XPKplayer(this);
        breakListener = new XPKbreak(this);
        explodeListener = new XPKarrgghh(this);
        pistonListener = new XPKPistonListener(this);
        pm.registerEvents(signListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(breakListener, this);
        pm.registerEvents(explodeListener, this);
        pm.registerEvents(pistonListener, this);
        trackPlayers = new ArrayList<String>();
        xpkExecutor = new XPKexecutor(this);
        getCommand("xpkgive").setExecutor(xpkExecutor);
        getCommand("xpkset").setExecutor(xpkExecutor);
        getCommand("xpkremove").setExecutor(xpkExecutor);
        getCommand("xpkforceremove").setExecutor(xpkExecutor);
        getCommand("xpkfist").setExecutor(xpkExecutor);
        getCommand("xpkedit").setExecutor(xpkExecutor);
        getCommand("xpkpay").setExecutor(xpkExecutor);
        getCommand("xpkwithdraw").setExecutor(xpkExecutor);
        getCommand("xpklimit").setExecutor(xpkExecutor);
        getCommand("xpkreload").setExecutor(xpkExecutor);
        getCommand("xpkcolour").setExecutor(xpkExecutor);
    }

    public int getKeptXP(UUID uuid, String w) {
        int keptXP = -1;
        try {
            Connection connection = service.getConnection();
            String queryXPGet = "SELECT amount FROM xpk WHERE uuid = ? AND world = ?";
            PreparedStatement statement = connection.prepareStatement(queryXPGet);
            statement.setString(1, uuid.toString());
            statement.setString(2, w);
            ResultSet rsget = statement.executeQuery();
            if (rsget.next()) {
                keptXP = rsget.getInt("amount");
            }
            rsget.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Could not GET XP: " + e);
        }
        return keptXP;
    }

    public void setKeptXP(double a, UUID uuid, String w) {
        try {
            Connection connection = service.getConnection();
            String queryXPSet = "UPDATE xpk SET amount = ? WHERE uuid = ? AND world = ?";
            PreparedStatement statement = connection.prepareStatement(queryXPSet);
            statement.setDouble(1, a);
            statement.setString(2, uuid.toString());
            statement.setString(3, w);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Could not SET XP: " + e);
        }
    }

    public void insKeptXP(UUID uuid, String w) {
        try {
            Connection connection = service.getConnection();
            String queryXPInsert = "INSERT INTO xpk (uuid, world, amount) VALUES (?, ?, 0)";
            PreparedStatement statement = connection.prepareStatement(queryXPInsert);
            statement.setString(1, uuid.toString());
            statement.setString(2, w);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Could not add new database record: " + e);
        }
    }

    public void delKeptXP(UUID uuid, String w) {
        try {
            Connection connection = service.getConnection();
            String queryXPDelete = "DELETE FROM xpk WHERE uuid = ? AND world = ?";
            PreparedStatement statement = connection.prepareStatement(queryXPDelete);
            statement.setString(1, uuid.toString());
            statement.setString(2, w);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Could not delete database record: " + e);
        }
    }

    public boolean isPlayersXPKSign(UUID uuid, String world, String nameOnSign) {
        boolean chk = false;
        String alias = getServer().getPlayer(uuid).getName();
        if (alias.length() > 15) {
            alias = alias.substring(0, 14);
        }
        if (nameOnSign.equals(alias)) {
            try {
                Connection connection = service.getConnection();
                String queryUUIDGet = "SELECT uuid FROM xpk WHERE uuid = ? AND world = ?";
                PreparedStatement statement = connection.prepareStatement(queryUUIDGet);
                statement.setString(1, uuid.toString());
                statement.setString(2, world);
                ResultSet rsget = statement.executeQuery();
                if (rsget.isBeforeFirst()) {
                    chk = true;
                }
                rsget.close();
                statement.close();
            } catch (SQLException e) {
                System.err.println("[XPKeeper] Could not GET XP: " + e);
            }
        }
        return chk;
    }

    public BlockFace getFace(Block b) {
        Sign s = (Sign) b.getState().getData();
        BlockFace bf = s.getAttachedFace();
        return bf;
    }

    public String stripColourCode(String s) {
        if (s.startsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
            return s.substring(2);
        } else {
            return s;
        }
    }

    /**
     * Closes the database.
     */
    private void closeDatabase() {
        try {
            service.connection.close();
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Could not close database connection: " + e);
        }
    }

    /**
     * Get if CraftBukkit version is 1.8 or higher
     *
     * @return true if version is >= 1.8
     */
    public boolean is1_8() {
        String name = getServer().getClass().getPackage().getName();
        String v = name.substring(name.lastIndexOf('.') + 1);
        String[] versions = v.split("_");
        return (versions[0].equals("v1") && Integer.parseInt(versions[1]) >= 8);
    }
}
