package dev.nexoria.database;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class DatabaseManager {

    private final NexoriaDatabasePlugin plugin;
    private Connection connection;
    private String databaseType;

    public DatabaseManager(NexoriaDatabasePlugin plugin) {
        this.plugin = plugin;
        connect();
    }

    public synchronized void connect() {
        close();
        this.databaseType = plugin.getConfig().getString("storage.type", "SQLITE").toUpperCase();

        try {
            if ("MYSQL".equalsIgnoreCase(databaseType)) {
                String host = plugin.getConfig().getString("storage.mysql.host", "localhost");
                int port = plugin.getConfig().getInt("storage.mysql.port", 3306);
                String db = plugin.getConfig().getString("storage.mysql.database", "minecraft");
                String user = plugin.getConfig().getString("storage.mysql.username", "root");
                String pass = plugin.getConfig().getString("storage.mysql.password", "");
                String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true";
                this.connection = DriverManager.getConnection(url, user, pass);
            } else {
                this.databaseType = "SQLITE";
                String relativePath = plugin.getConfig().getString("storage.sqlite.file", "data/storage.db");
                File dbFile = new File(plugin.getDataFolder(), relativePath);
                if (!dbFile.getParentFile().exists()) {
                    dbFile.getParentFile().mkdirs();
                }
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                this.connection = DriverManager.getConnection(url);
            }

            initTables();
            plugin.getLogger().info("Successfully established connection to " + databaseType + " database.");
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to initialize database connection: " + e.getMessage());
        }
    }

    private void initTables() {
        executeAsync("CREATE TABLE IF NOT EXISTS nexoria_metadata (meta_key VARCHAR(128) PRIMARY KEY, meta_value TEXT);");
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public CompletableFuture<Integer> executeAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                return ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Error executing async SQL update: " + e.getMessage());
                return -1;
            }
        });
    }

    public <T> CompletableFuture<T> queryAsync(String sql, Function<ResultSet, T> mapper, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return mapper.apply(rs);
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Error executing async SQL query: " + e.getMessage());
                return null;
            }
        });
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {}
            connection = null;
        }
    }
}
