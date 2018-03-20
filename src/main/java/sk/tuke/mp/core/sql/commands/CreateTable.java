package sk.tuke.mp.core.sql.commands;

import sk.tuke.mp.core.sql.Field;
import sk.tuke.mp.core.sql.Table;
import sk.tuke.mp.core.sql.decorators.AdditionalDecorator;
import sk.tuke.mp.core.sql.decorators.DialectDependentDecorator;
import sk.tuke.mp.utils.Semantics;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
        return String.format("CREATE TABLE %s (\n", Semantics.escapeReference(table.getName(), dialect))
            + table.getFields().stream()
                .map(f -> Semantics.formatField(f, dialect)) // Field::toString)
                .collect(Collectors.joining(",\n","",","))
            + table.getFields().stream()
                .filter(x -> x instanceof AdditionalDecorator)
                .map(Field::toStringDecoration)
                .collect(Collectors.joining(",\n"))
            + String.format("%s\n", table instanceof DialectDependentDecorator ? ",\n" + (((DialectDependentDecorator) table).toStringDecoration(dialect)) : "")
            + ")" + Semantics.getClosingChar(dialect);
    }

    @Override
    public int execute(Connection conn) throws SQLException {
        // get SQL
        String cmd = getQuery();
        // check if table exists
        String tableName = table.getName().toUpperCase();
        DatabaseMetaData dmd = conn.getMetaData();
        ResultSet rs = dmd.getTables(null,null, tableName,null);
        if (!rs.next()) {
            // no old table, create new one
            return conn.createStatement().execute(cmd) ? 1 : 0;
        } else {
            // first drop the old table
            try {
                // first drop exported constraints
                ResultSet rsConstraints = dmd.getExportedKeys(null, null, tableName);
                while (rsConstraints.next()) {
                    conn.createStatement().execute("ALTER TABLE " + rsConstraints.getObject("FKTABLE_NAME") + " DROP CONSTRAINT " + rsConstraints.getObject("FK_NAME"));
                }
                // drop old table
                conn.createStatement().execute("DROP TABLE " + tableName);
                // now create new one
                return conn.createStatement().execute(cmd) ? 1 : 0;
            } catch (Exception e) {
                throw new SQLException("Cannot create " + table.getName() + " table, because it already exists and cannot be dropped due to: \n" + e.getMessage());
            }
        }
    }
}
