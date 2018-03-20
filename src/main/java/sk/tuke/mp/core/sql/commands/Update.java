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

public class Update implements Command {

    private Table table;
    private Object object;
    private PersistenceMapper<Table> mapper;
    private Cache cache;

    public Update(Object object, PersistenceMapper<Table> mapper, Cache cache) {
        this.object = object;
        this.table = mapper.getUnit(object.getClass());
        this.mapper = mapper;
        this.cache = cache;
    }

    public String getQuery(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        // start SQL statement
        sb.append("UPDATE ")
                .append(table.getName())
                .append(" SET ");

        // gain values from object
        List<String> fieldsValues = new ArrayList<>();
        for(Field field : table.getFields()) {
            // TODO: ignore primary key??
            Object value = null;
            if(field.getOriginalName() != null) {
                // foreign key field
                Object foreignObject = ReflectionExtension.getPropertyValue(object, field.getOriginalName());
                // recursive update/insert FK object into the DB
                new InsertOrUpdate(foreignObject, mapper, cache).execute(conn);
                // get FK value
                Field foreignField = ((ForeignKeyTable)table).getForeignField(field);
                value = ReflectionExtension.getPropertyValue(foreignObject, foreignField.getName());
            } else {
                // normal field
                value = ReflectionExtension.getPropertyValue(object, field.getName());
            }
            // write column-value pair into the SQL statement
            if(value != null) {
                fieldsValues.add(new StringBuilder()
                        .append(field.getName())
                        .append("=")
                        .append(FieldTypes.escapeValue(value))
                        .toString());
            }
        }

        // set primary key criteria and close SQL statement
        Field primaryKey = table.getPrimaryKeyField();
        sb.append(String.join(", ", fieldsValues))
        .append(" WHERE ")
        .append(primaryKey.getName())
        .append("=")
        .append(ReflectionExtension.getPropertyValue(object, primaryKey.getName())) // getOriginalName()));
        .append(Semantics.getClosingChar(SQLDialects.detect(conn)));
        return sb.toString();
    }

    @Override
    public int execute(Connection conn) throws SQLException {
        String cmd = getQuery(conn);
        return conn.createStatement().executeUpdate(cmd);
    }
}
