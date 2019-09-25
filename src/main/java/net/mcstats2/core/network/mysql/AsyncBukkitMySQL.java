package net.mcstats2.core.network.mysql;

import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


public class AsyncBukkitMySQL implements AsyncMySQL {
    private ExecutorService executor;
    private Plugin plugin;
    private MySQL sql;

    public AsyncBukkitMySQL(Plugin owner, String host, int port, String user, String password, String database) {
        try {
            sql = new MySQL(host, port, user, password, database);
            executor = Executors.newCachedThreadPool();
            plugin = owner;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(PreparedStatement statement) {
        executor.execute(() -> sql.queryUpdate(statement));
    }

    @Override
    public void update(String statement) {
        executor.execute(() -> sql.queryUpdate(statement));
    }

    @Override
    public void update(String statement, Object... args) {
        executor.execute(() -> sql.queryUpdate(statement, args));
    }

    @Override
    public void update(PreparedStatement statement, Consumer<Integer> consumer) {
        executor.execute(() -> {
            int result = sql.queryUpdate(statement);
            plugin.getServer().getScheduler().runTask(plugin, () -> consumer.accept(result));
        });
    }

    @Override
    public void update(String statement, Consumer<Integer> consumer) {
        executor.execute(() -> {
            int result = sql.queryUpdate(statement);
            plugin.getServer().getScheduler().runTask(plugin, () -> consumer.accept(result));
        });
    }

    @Override
    public void update(String statement, Consumer<Integer> consumer, Object... args) {
        executor.execute(() -> {
            int result = sql.queryUpdate(statement, args);
            plugin.getServer().getScheduler().runTask(plugin, () -> consumer.accept(result));
        });
    }

    @Override
    public void query(PreparedStatement statement, Consumer<ResultSet> consumer) {
        executor.execute(() -> {
            ResultSet result = sql.query(statement);
            plugin.getServer().getScheduler().runTask(plugin, () -> consumer.accept(result));
        });
    }

    @Override
    public void query(String statement, Consumer<ResultSet> consumer) {
        executor.execute(() -> {
            ResultSet result = sql.query(statement);
            plugin.getServer().getScheduler().runTask(plugin, () -> consumer.accept(result));
        });
    }

    @Override
    public void query(String statement, Consumer<ResultSet> consumer, Object... args) {
        executor.execute(() -> {
            ResultSet result = sql.query(statement, args);
            plugin.getServer().getScheduler().runTask(plugin, () -> consumer.accept(result));
        });
    }

    @Override
    public PreparedStatement prepare(String query) {
        try {
            return sql.getConnection().prepareStatement(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MySQL getMySQL() {
        return sql;
    }
}