package me.eccentric_nz.plugins.xpkeeper;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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
    public HashMap<String, Boolean> trackPlayers;

    @Override
    public void onDisable() {
        // TODO: Place any custom disable code here.
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        signListener = new XPKsign(this);
        playerListener = new XPKplayer(this);
        breakListener = new XPKbreak(this);
        explodeListener = new XPKarrgghh(this);
        pistonListener = new XPKPistonListener(this);
        pm = getServer().getPluginManager();
        pm.registerEvents(signListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(breakListener, this);
        pm.registerEvents(explodeListener, this);
        pm.registerEvents(pistonListener, this);
        service = XPKdatabase.getInstance();
        try {
            String path = getDataFolder() + File.separator + "XPKeeper.db";
            service.setConnection(path);
            service.createTable();
        } catch (Exception e) {
            System.err.println("[XPKeeper] Connection and Tables Error: " + e);
        }
        XPKconfig xpkc = new XPKconfig(this);
        xpkc.checkConfig();
        trackPlayers = new HashMap<String, Boolean>();
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

    public int getKeptXP(String p, String w) {
        int keptXP = -1;
        try {
            Connection connection = service.getConnection();
            String queryXPGet = "SELECT amount FROM xpk WHERE player = ? AND world = ?";
            PreparedStatement statement = connection.prepareStatement(queryXPGet);
            statement.setString(1, p);
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

    public void setKeptXP(double a, String p, String w) {
        try {
            Connection connection = service.getConnection();
            String queryXPSet = "UPDATE xpk SET amount = ? WHERE player = ? AND world = ?";
            PreparedStatement statement = connection.prepareStatement(queryXPSet);
            statement.setDouble(1, a);
            statement.setString(2, p);
            statement.setString(3, w);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Could not SET XP: " + e);
        }
    }

    public void insKeptXP(String p, String w) {
        try {
            Connection connection = service.getConnection();
            String queryXPInsert = "INSERT INTO xpk (player,world,amount) VALUES (?,?,0)";
            PreparedStatement statement = connection.prepareStatement(queryXPInsert);
            statement.setString(1, p);
            statement.setString(2, w);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Could not add new database record: " + e);
        }
    }

    public void delKeptXP(String p, String w) {
        try {
            Connection connection = service.getConnection();
            String queryXPDelete = "DELETE FROM xpk WHERE player = ? AND world= ?";
            PreparedStatement statement = connection.prepareStatement(queryXPDelete);
            statement.setString(1, p);
            statement.setString(2, w);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Could not delete database record: " + e);
        }
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
}
