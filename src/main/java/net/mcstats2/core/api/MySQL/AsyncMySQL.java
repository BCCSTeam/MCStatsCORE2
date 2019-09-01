package net.mcstats2.core.api.MySQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.function.Consumer;

public interface AsyncMySQL {

    void update(PreparedStatement statement);

    void update(String statement);

    void update(String statement, List<Object> args);

    void update(PreparedStatement statement, Consumer<Integer> consumer);

    void update(String statement, Consumer<Integer> consumer);

    void update(String statement, List<Object> args, Consumer<Integer> consumer);

    void query(PreparedStatement statement, Consumer< ResultSet > consumer);

    void query(String statement, Consumer<ResultSet> consumer);

    void query(String statement, List<Object> args, Consumer<ResultSet> consumer);

    PreparedStatement prepare(String query);

    MySQL getMySQL();
}
