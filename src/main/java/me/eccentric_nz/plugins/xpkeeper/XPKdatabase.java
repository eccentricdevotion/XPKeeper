package me.eccentric_nz.plugins.xpkeeper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class XPKdatabase {

    private static final XPKdatabase instance = new XPKdatabase();
    public Connection connection = null;
    public Statement statement;

    public static synchronized XPKdatabase getInstance() {
        return instance;
    }

    public void setConnection(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTable() {
        try {
            statement = connection.createStatement();
            String queryXPK = "CREATE TABLE IF NOT EXISTS xpk (xpk_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, player TEXT COLLATE NOCASE, world TEXT, amount REAL)";
            statement.executeUpdate(queryXPK);
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Create table error: " + e);
        }
    }
}
