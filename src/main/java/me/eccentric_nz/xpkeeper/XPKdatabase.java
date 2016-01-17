package me.eccentric_nz.xpkeeper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class XPKdatabase {

    private static final XPKdatabase INSTANCE = new XPKdatabase();
    public Connection connection = null;
    public Statement statement;

    public static synchronized XPKdatabase getInstance() {
        return INSTANCE;
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
            String queryXPK = "CREATE TABLE IF NOT EXISTS xpk (xpk_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, uuid TEXT, player TEXT COLLATE NOCASE, world TEXT, amount REAL)";
            statement.executeUpdate(queryXPK);
            // update inventories if there is no uuid column
            String queryUUID = "SELECT sql FROM sqlite_master WHERE tbl_name = 'xpk' AND sql LIKE '%uuid TEXT%'";
            ResultSet rsUUID = statement.executeQuery(queryUUID);
            if (!rsUUID.next()) {
                String queryAlterU = "ALTER TABLE xpk ADD uuid TEXT";
                statement.executeUpdate(queryAlterU);
                System.out.println("[XPKeeper] Adding UUID to database!");
            }
        } catch (SQLException e) {
            System.err.println("[XPKeeper] Create table error: " + e);
        }
    }
}
