package me.eccentric_nz.xpkeeper;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.logging.Level;

public class XPKDatabase {

    private static final XPKDatabase INSTANCE = new XPKDatabase();
    public Connection connection = null;
    public Statement statement;

    public static synchronized XPKDatabase getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public void createTable() {
        try {
            statement = connection.createStatement();
            String queryXPK = "CREATE TABLE IF NOT EXISTS xpk (xpk_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, uuid TEXT, player TEXT COLLATE NOCASE, world TEXT, sign TEXT, amount REAL)";
            statement.executeUpdate(queryXPK);
            // update xpk if there is no uuid column
            String queryUUID = "SELECT sql FROM sqlite_master WHERE tbl_name = 'xpk' AND sql LIKE '%uuid TEXT%'";
            ResultSet rsUUID = statement.executeQuery(queryUUID);
            if (!rsUUID.next()) {
                String queryAlterU = "ALTER TABLE xpk ADD uuid TEXT";
                statement.executeUpdate(queryAlterU);
                Bukkit.getLogger().log(Level.INFO, " Adding UUID to database!");
            }
            // update xpk if there is no sign column
            String querySign = "SELECT sql FROM sqlite_master WHERE tbl_name = 'xpk' AND sql LIKE '%sign TEXT%'";
            ResultSet rsSign = statement.executeQuery(querySign);
            if (!rsSign.next()) {
                String queryAlterU = "ALTER TABLE xpk ADD sign TEXT";
                statement.executeUpdate(queryAlterU);
                Bukkit.getLogger().log(Level.INFO, " Adding Sign UUID to database!");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.INFO, "Create table error: " + e);
        }
    }
}
