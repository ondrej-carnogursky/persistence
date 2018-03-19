package sk.tuke.mp.core.sql.commands;

import sk.tuke.mp.core.PersistenceMapper;
import sk.tuke.mp.core.sql.Field;
import sk.tuke.mp.core.sql.FieldTypes;
import sk.tuke.mp.core.sql.Table;
import sk.tuke.mp.core.sql.decorators.ForeignKeyTable;
import sk.tuke.mp.utils.ReflectionExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Insert implements Command {

    private Table table;
    private Object object;
    private PersistenceMapper<Table> mapper;

    public Insert(Object object, PersistenceMapper<Table> mapper) {
        this.object = object;
        this.table = mapper.getUnit(object.getClass());
        this.mapper = mapper;
    }


    //@Override
    public String getQuery(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ")
                .append(this.table.getName())
                .append("(");

        List<String> fieldNames = new ArrayList<>();
        List<String> fieldValues = new ArrayList<>();
        table.getFields().stream()
        .filter(f -> f.getName() == f.getOriginalName())
        .forEach(f -> {
            fieldNames.add(f.getName());
            Object value = ReflectionExtension.getPropertyValue(object, f.getName());
            fieldValues.add(FieldTypes.escapeValue(value));
        });
        if(table instanceof ForeignKeyTable) {
            ForeignKeyTable fkTable = (ForeignKeyTable)table;
            for(Field f: fkTable.getMyFields()) {
                Object foreignObject = ReflectionExtension.getPropertyValue(object, f.getOriginalName());
                new InsertOrUpdate(foreignObject, mapper).execute(conn);
                Field foreignField = fkTable.getForeignField(f);
                Object foreignValue = ReflectionExtension.getPropertyValue(foreignObject, foreignField.getName());
                fieldNames.add(f.getName());
                fieldValues.add(FieldTypes.escapeValue(foreignValue));
            }
        }
        sb.append(String.join(", ", fieldNames));
        sb.append(") VALUES(");
        sb.append(String.join(", ", fieldValues));
        sb.append(");");

        return sb.toString();
    }

    @Override
    public int execute(Connection conn) throws SQLException {
        String cmd = getQuery(conn);
        return conn.createStatement().executeUpdate(cmd);
    }
}
