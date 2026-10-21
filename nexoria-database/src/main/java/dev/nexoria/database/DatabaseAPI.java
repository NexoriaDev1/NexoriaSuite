package dev.nexoria.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Public developer API for NexoriaDatabase.
 */
public class DatabaseAPI {

    private final NexoriaDatabasePlugin plugin;

    public DatabaseAPI(NexoriaDatabasePlugin plugin) {
        this.plugin = plugin;
    }

    public Connection getConnection() throws SQLException {
        return plugin.getDatabaseManager().getConnection();
    }

    public boolean isConnected() {
        return plugin.getDatabaseManager().isConnected();
    }

    public CompletableFuture<Integer> executeAsync(String sql, Object... params) {
        return plugin.getDatabaseManager().executeAsync(sql, params);
    }

    public <T> CompletableFuture<T> queryAsync(String sql, Function<ResultSet, T> mapper, Object... params) {
        return plugin.getDatabaseManager().queryAsync(sql, mapper, params);
    }

    public DatabaseManager getManager() {
        return plugin.getDatabaseManager();
    }
}
