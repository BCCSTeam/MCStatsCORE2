package net.mcstats2.core.network.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Consumer;

public interface AsyncMySQL {

    void update(PreparedStatement statement);

    void update(String statement);

    void update(String statement, Object... args);

    void update(PreparedStatement statement, Consumer<Integer> consumer);

    void update(String statement, Consumer<Integer> consumer);

    void update(String statement, Consumer<Integer> consumer, Object... args);

    void query(PreparedStatement statement, Consumer<ResultSet> consumer);

    void query(String statement, Consumer<ResultSet> consumer);

    void query(String statement, Consumer<ResultSet> consumer, Object... args);

    PreparedStatement prepare(String query);

    MySQL getMySQL();
}
