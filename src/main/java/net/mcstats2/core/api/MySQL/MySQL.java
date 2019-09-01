package net.mcstats2.core.api.MySQL;


import java.sql.*;
import java.util.List;

public class MySQL {

    private String host, user, password, database;
    private int port;

    private Connection conn;

    public MySQL(String host, int port, String user, String password, String database) throws Exception {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;

        this.openConnection();
    }

    public int queryUpdate(String query) {
        checkConnection();
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            return queryUpdate(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int queryUpdate(String query, Object... args) {
        checkConnection();
        try (PreparedStatement statement = conn.prepareStatement(query)) {

            for(int i=0;i<args.length;i++)
                statement.setObject(1+i, args[i]);

            return queryUpdate(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int queryUpdate(PreparedStatement statement) {
        checkConnection();
        try {
            return statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    public ResultSet query(String query) {
        checkConnection();
        try {
            return query(conn.prepareStatement(query));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet query(String query, Object... args) {
        checkConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(query);

            for(int i=0;i<args.length;i++)
                ps.setObject(1+i, args[i]);

            return query(ps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet query(PreparedStatement statement) {
        checkConnection();
        try {
            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isConnected() {
        try {
            return this.conn != null && this.conn.isValid(10) && !this.conn.isClosed();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public Connection getConnection() {
        return this.conn;
    }

    private void checkConnection() {
        try {
            if (this.conn == null || !this.conn.isValid(10) || this.conn.isClosed()) openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection openConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return this.conn = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password);
    }

    public void closeConnection() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.conn = null;
        }
    }
}