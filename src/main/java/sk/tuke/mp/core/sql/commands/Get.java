package sk.tuke.mp.core.sql.commands;

import sk.tuke.mp.core.PersistenceMapper;
import sk.tuke.mp.core.sql.Field;
import sk.tuke.mp.core.sql.FieldTypes;
import sk.tuke.mp.core.sql.Table;
import sk.tuke.mp.persistence.Cache;
import sk.tuke.mp.utils.ReflectionExtension;
import sk.tuke.mp.utils.Semantics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Get implements Query {

    private Table table;
    private Class type;
    private PersistenceMapper<Table> mapper;
    private Cache cache;
    private Integer id;
    private HashMap<String, Object> criteria = new HashMap();

    public Get(Class type, PersistenceMapper<Table> mapper, Cache cache, Object... args) {
        this.type = type;
        this.table = mapper.getUnit(type);
        this.mapper = mapper;
        this.cache = cache;
        if(args.length == 1) {
            // set primary key criteria for query
            criteria.put(table.getPrimaryKeyField().getName(), args[0]);
        } else {
            // set column & value criteria for query
            for(int i = 0; i < args.length; i += 2) {
                if(i + 1 == args.length) return;
                criteria.put((String)args[i], args[i + 1]);
            }
        }
    }

    private String getQuery(Connection conn) {
        StringBuilder sb = new StringBuilder();
        // start SQL statement
        sb.append("SELECT * ")
        .append(" FROM ")
        .append(this.table.getName());

        // set criteria for query
        if(criteria.size() > 0) {
            sb.append(" WHERE ");
            List<String> crits = new ArrayList<>();
            for (Map.Entry crit : criteria.entrySet()) {
                crits.add(crit.getKey() + "=" + FieldTypes.escapeValue(crit.getValue()));
            }
            sb.append(String.join(" AND ", crits));
        }

        // close SQL statement
        sb.append(Semantics.getClosingChar(SQLDialects.detect(conn)));
        return sb.toString();
    }

    @Override
    public List<Object> executeQuery(Connection conn) throws SQLException {
        // get ResultSet from DB
        String cmd = getQuery(conn);
        ResultSet rs = conn.createStatement().executeQuery(cmd);
        // generate Objects
        List<Object> list = new ArrayList<>();
        while (rs.next()) {
            try {
                // check if was cached
                Field idField = table.getPrimaryKeyField();
                Object idValue = rs.getObject(idField.getName());
                Object obj = cache.getById(type, idValue);
                if(obj == null) {
                    // not cached yet, so create new instance, set id property and cache it
                    obj = type.newInstance();
                    ReflectionExtension.setPropertyValue(obj, idField.getName(), idValue);
                    cache.tryPutInstance(obj);
                }
                // copy remaining fields from DB to Object
                List<Field> fields = table.getFields();
                for(Field f: fields) {
                    if(f.getOriginalName() == null) {
                        if (f != idField)
                            // normal field
                            ReflectionExtension.setPropertyValue(obj, f.getName(), rs.getObject(f.getName()));
                    } else {
                        // foreign key field
                        Object foreignKeyValue =  rs.getObject(f.getName());
                        // get FK table
                        java.lang.reflect.Field reflectField = type.getDeclaredField(f.getOriginalName());
                        Class foreignType = reflectField.getType();
                        // recursive get FK object from FK table
                        List<Object> foreignObjects = new Get(foreignType, mapper, cache, foreignKeyValue).executeQuery(conn);
                        if(foreignObjects.size() > 0)
                            // set foreign key field to FK object
                            ReflectionExtension.setPropertyValue(obj, reflectField, foreignObjects.get(0));
                        else
                            // TODO: check original foreign field and clean cache if needed ??
                            // FK object not found, set null for foreign key field
                            ReflectionExtension.setPropertyValue(obj, reflectField, null);
                            // throw new SQLException("Non existing entry in " + foreignTable.getName() + " foreign table for field " + f.getName() + "=" + foreignKeyValue.toString() + ".");
                    }
                }
                list.add(obj);
            } catch(Exception e) {
                throw new SQLException("Something went wrong, see: \"" + e.getMessage() + "\"");
            }
        }
        return list;
    }

 }
