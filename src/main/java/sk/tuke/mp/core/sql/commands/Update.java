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

public class Update implements Command {

    private Table table;
    private Object object;
    private PersistenceMapper<Table> mapper;

    public Update(Object object, PersistenceMapper<Table> mapper) {
        this.object = object;
        this.table = mapper.getUnit(object.getClass());
        this.mapper = mapper;
    }

    public String getQuery(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ")
                .append(table.getName())
                .append(" SET ");

        List<String> fieldsValues = new ArrayList<>();

        if(table instanceof ForeignKeyTable) {
            for(Field f : ((ForeignKeyTable)table).getMyFields()) {
                Object foreignObject = ReflectionExtension.getPropertyValue(object, f.getOriginalName());
                new InsertOrUpdate(foreignObject, mapper).execute(conn);

            }
        }

        for(Field field : table.getFields()) {
            Object value = ReflectionExtension.getPropertyValue(object, field.getOriginalName());
            if(table instanceof ForeignKeyTable && ((ForeignKeyTable)table).isForeignKey(field)) {
                Field foreignField = ((ForeignKeyTable)table).getForeignField(field);
                value = ReflectionExtension.getPropertyValue(value, foreignField.getName());
            }

            if(value != null) {
                fieldsValues.add(new StringBuilder()
                        .append(field.getName())
                        .append("=")
                        .append(FieldTypes.escapeValue(value))
                        .toString());
            }
        }

        Field primaryKey = table.getPrimaryKeyField();

        sb.append(String.join(", ", fieldsValues))
        .append(" WHERE ")
        .append(primaryKey.getName())
        .append("=")
        .append(ReflectionExtension.getPropertyValue(object, primaryKey.getOriginalName()));
        return sb.toString();
    }

    @Override
    public int execute(Connection conn) throws SQLException {
        String cmd = getQuery(conn);
        return conn.createStatement().executeUpdate(cmd);
    }
}
