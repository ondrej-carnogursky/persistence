package sk.tuke.mp.core.sql.commands;

import sk.tuke.mp.core.PersistenceMapper;
import sk.tuke.mp.core.sql.Field;
import sk.tuke.mp.core.sql.FieldTypes;
import sk.tuke.mp.core.sql.Table;
import sk.tuke.mp.core.sql.decorators.ForeignKeyTable;
import sk.tuke.mp.persistence.Cache;
import sk.tuke.mp.utils.ReflectionExtension;
import sk.tuke.mp.utils.Semantics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Insert implements Command {

    private Table table;
    private Object object;
    private PersistenceMapper<Table> mapper;
    private Cache cache;

    public Insert(Object object, PersistenceMapper<Table> mapper, Cache cache) {
        this.object = object;
        this.table = mapper.getUnit(object.getClass());
        this.mapper = mapper;
        this.cache = cache;
    }

    public String getQuery(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        // start SQL statement
        sb.append("INSERT INTO ")
                .append(this.table.getName())
                .append("(");

        // gain values from object
        List<String> fieldNames = new ArrayList<>();
        List<String> fieldValues = new ArrayList<>();
        table.getFields().stream()
        .filter(f -> f.getOriginalName() == null) // normal fields
        .forEach(f -> {
            // TODO: autoincremented primary key??
            fieldNames.add(f.getName());
            Object value = ReflectionExtension.getPropertyValue(object, f.getName());
            fieldValues.add(FieldTypes.escapeValue(value));
        });
        if(table instanceof ForeignKeyTable) {
            ForeignKeyTable fkTable = (ForeignKeyTable)table;
            // foreign key fields
            for(Field f: fkTable.getMyFields()) {
                Object foreignObject = ReflectionExtension.getPropertyValue(object, f.getOriginalName());
                // recursive update/insert FK object into the DB
                new InsertOrUpdate(foreignObject, mapper, cache).execute(conn);
                // get FK value
                Field foreignField = fkTable.getForeignField(f);
                Object foreignValue = ReflectionExtension.getPropertyValue(foreignObject, foreignField.getName());
                // write to names/values lists
                fieldNames.add(f.getName());
                fieldValues.add(FieldTypes.escapeValue(foreignValue));
            }
        }
        // write columns & values into the SQL statement and close it
        sb.append(String.join(", ", fieldNames));
        sb.append(") VALUES(");
        sb.append(String.join(", ", fieldValues));
        sb.append(")" + Semantics.getClosingChar(SQLDialects.detect(conn)));

        return sb.toString();
    }

    @Override
    public int execute(Connection conn) throws SQLException {
        String cmd = getQuery(conn);
        return conn.createStatement().executeUpdate(cmd);
    }
}
