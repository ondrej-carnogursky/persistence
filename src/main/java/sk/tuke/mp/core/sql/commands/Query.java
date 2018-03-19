package sk.tuke.mp.core.sql.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface Query {
    List<Object> executeQuery(Connection conn) throws SQLException;
}
