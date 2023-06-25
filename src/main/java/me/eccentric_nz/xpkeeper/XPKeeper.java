package me.eccentric_nz.xpkeeper;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class XPKeeper extends JavaPlugin {

    public List<UUID> trackPlayers;
    public List<UUID> trackUpdaters;
    public List<UUID> trackOps;
    private XPKDatabase service;
    private PluginManager pm;
    private XPKExecutor xpkExecutor;
    private PersistentDataType<byte[], UUID> persistentDataTypeUUID;
    private NamespacedKey nskPlayer;
    private NamespacedKey nskSign;

    @Override
    public void onDisable() {
        closeDatabase();
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        XPKConfig xpkc = new XPKConfig(this);
        xpkc.checkConfig();
        service = XPKDatabase.getInstance();
        try {
            String path = getDataFolder() + File.separator + "XPKeeper.db";
            service.setConnection(path);
            service.createTable();
        } catch (Exception e) {
            getLogger().log(Level.INFO, "Connection and Tables Error: " + e);
        }
        pm = getServer().getPluginManager();
        persistentDataTypeUUID = new XPKUuid();
        nskPlayer = new NamespacedKey(this, "uuid_player");
        nskSign = new NamespacedKey(this, "uuid_sign");
        pm.registerEvents(new XPKSign(this), this);
        pm.registerEvents(new XPKPlayer(this), this);
        pm.registerEvents(new XPKBreak(this), this);
        pm.registerEvents(new XPKArrgghh(this), this);
        pm.registerEvents(new XPKPistonListener(this), this);
        trackPlayers = new ArrayList<>();
        trackUpdaters = new ArrayList<>();
        trackOps = new ArrayList<>();
        xpkExecutor = new XPKExecutor(this);
        getCommand("xpkgive").setExecutor(xpkExecutor);
        getCommand("xpkset").setExecutor(xpkExecutor);
        getCommand("xpkremove").setExecutor(xpkExecutor);
        getCommand("xpkupdate").setExecutor(xpkExecutor);
        getCommand("xpkforceremove").setExecutor(xpkExecutor);
        getCommand("xpkfist").setExecutor(xpkExecutor);
        getCommand("xpkedit").setExecutor(xpkExecutor);
        getCommand("xpkpay").setExecutor(xpkExecutor);
        getCommand("xpkwithdraw").setExecutor(xpkExecutor);
        getCommand("xpklimit").setExecutor(xpkExecutor);
        getCommand("xpkreload").setExecutor(xpkExecutor);
        getCommand("xpkcolour").setExecutor(xpkExecutor);
    }

    public PersistentDataType<byte[], UUID> getPersistentDataTypeUUID() {
        return persistentDataTypeUUID;
    }

    public NamespacedKey getNskPlayer() {
        return nskPlayer;
    }

    public NamespacedKey getNskSign() {
        return nskSign;
    }

    public int getKeptXP(UUID uuid, String w, String signUuid) {
        int keptXP = -1;
        try {
            Connection connection = service.getConnection();
            String queryXPGet = "SELECT amount FROM xpk WHERE uuid = ? AND world = ? AND sign = ?";
            PreparedStatement statement = connection.prepareStatement(queryXPGet);
            statement.setString(1, uuid.toString());
            statement.setString(2, w);
            statement.setString(3, signUuid);
            ResultSet rsGet = statement.executeQuery();
            if (rsGet.next()) {
                keptXP = rsGet.getInt("amount");
            }
            rsGet.close();
            statement.close();
        } catch (SQLException e) {
            getLogger().log(Level.INFO, "Could not GET XP: " + e);
        }
        return keptXP;
    }

    public void setKeptXP(double a, UUID uuid, String w, String signUuid) {
        try {
            Connection connection = service.getConnection();
            String queryXPSet = "UPDATE xpk SET amount = ? WHERE uuid = ? AND world = ? AND sign = ?";
            PreparedStatement statement = connection.prepareStatement(queryXPSet);
            statement.setDouble(1, a);
            statement.setString(2, uuid.toString());
            statement.setString(3, w);
            statement.setString(4, signUuid);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            getLogger().log(Level.INFO, "Could not SET XP: " + e);
        }
    }

    public void insKeptXP(UUID uuid, String w, String playerName, String signUuid) {
        try {
            Connection connection = service.getConnection();
            String queryXPInsert = "INSERT INTO xpk (uuid, player, world, sign, amount) VALUES (?, ?, ?, ?, 0)";
            PreparedStatement statement = connection.prepareStatement(queryXPInsert);
            statement.setString(1, uuid.toString());
            statement.setString(2, playerName);
            statement.setString(3, w);
            statement.setString(4, signUuid);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            getLogger().log(Level.INFO, "Could not add new database record: " + e);
        }
    }

    public void delKeptXP(UUID uuid, String w, String signUuid) {
        try {
            Connection connection = service.getConnection();
            String queryXPDelete = "DELETE FROM xpk WHERE uuid = ? AND world = ? AND sign = ?";
            PreparedStatement statement = connection.prepareStatement(queryXPDelete);
            statement.setString(1, uuid.toString());
            statement.setString(2, w);
            statement.setString(3, signUuid);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            getLogger().log(Level.INFO, "Could not delete database record: " + e);
        }
    }

    public boolean isPlayersXPKSign(Sign sign, UUID uuid, String world, String nameOnSign) {
        boolean chk = false;
        PersistentDataContainer pdc = sign.getPersistentDataContainer();
        if (pdc.has(nskPlayer, persistentDataTypeUUID)) {
            UUID storedUuid = pdc.get(nskPlayer, persistentDataTypeUUID);
            chk = storedUuid != null && storedUuid.equals(uuid);
        } else {
            String alias = getServer().getPlayer(uuid).getName();
            if (alias.startsWith(nameOnSign)) {
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
                    getLogger().log(Level.INFO, "Could not GET XP: " + e);
                }
            } else {
                // name may have changed - check last known name (player field in db)
                try {
                    Connection connection = service.getConnection();
                    String queryLKN = "SELECT player FROM xpk WHERE uuid = ? AND world = ?";
                    PreparedStatement statement = connection.prepareStatement(queryLKN);
                    statement.setString(1, uuid.toString());
                    statement.setString(2, world);
                    ResultSet rsLKN = statement.executeQuery();
                    if (rsLKN.isBeforeFirst()) {
                        rsLKN.next();
                        String lkn = rsLKN.getString("player");
                        if (lkn.startsWith(nameOnSign)) {
                            chk = true;
                            // update player field in db
                            String queryUpdate = "UPDATE xpk SET player = ? WHERE uuid = ? AND world = ?";
                            PreparedStatement ps = connection.prepareStatement(queryUpdate);
                            ps.setString(1, alias);
                            ps.setString(2, uuid.toString());
                            ps.setString(3, world);
                            ps.executeUpdate();
                            ps.close();
                        }
                    }
                    rsLKN.close();
                    statement.close();
                } catch (SQLException e) {
                    getLogger().log(Level.INFO, "Could not GET XP (from last known player name): " + e);
                }
            }
        }
        return chk;
    }

    public void updateXPKRecord(UUID uuid, String world, String signUuid) {
        try {
            Connection connection = service.getConnection();
            // update sign field in db if it is empty
            String queryUpdate = "UPDATE xpk SET sign = ? WHERE uuid = ? AND world = ? AND sign = ''";
            PreparedStatement ps = connection.prepareStatement(queryUpdate);
            ps.setString(1, signUuid);
            ps.setString(2, uuid.toString());
            ps.setString(3, world);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            getLogger().log(Level.INFO, "Could not GET XP (from last known player name): " + e);
        }
    }

    /**
     * Closes the database.
     */
    private void closeDatabase() {
        try {
            service.connection.close();
        } catch (SQLException e) {
            getLogger().log(Level.INFO, "Could not close database connection: " + e);
        }
    }
}
