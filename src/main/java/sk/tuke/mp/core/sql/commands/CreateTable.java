package sk.tuke.mp.core.sql.commands;

import sk.tuke.mp.core.sql.Field;
import sk.tuke.mp.core.sql.Table;
import sk.tuke.mp.core.sql.decorators.AdditionalDecorator;
import sk.tuke.mp.core.sql.decorators.DialectDependentDecorator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class CreateTable implements Command {

    private Table table;
    private SQLDialects dialect;

    public CreateTable(Table table, SQLDialects dialect) {
        this.table = table;
        this.dialect = dialect;
    }

    //@Override
    public String getQuery() {
        return String.format("CREATE TABLE IF NOT EXISTS %c%s%c (\n", '\"', table.getName(), '\"')
            + table.getFields().stream()
                .map(Field::toString)
                .collect(Collectors.joining(",\n","",","))
            + table.getFields().stream()
                .filter(x -> x instanceof AdditionalDecorator)
                .map(Field::toStringDecoration)
                .collect(Collectors.joining(",\n"))
            + String.format("%s\n", table instanceof DialectDependentDecorator ? ",\n" + (((DialectDependentDecorator) table).toStringDecoration(dialect)) : "")
            + ");";
    }

    @Override
    public int execute(Connection conn) throws SQLException {
        String cmd = getQuery();
        return conn.createStatement().execute(cmd) ? 1 : 0;
    }
}
