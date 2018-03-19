package sk.tuke.mp.core.sql.commands;

import java.sql.Connection;
import java.sql.SQLException;

public interface Command {

    int execute(Connection conn) throws SQLException;

}
