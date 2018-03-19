package sk.tuke.mp.core.sql.commands;

import sk.tuke.mp.core.PersistenceMapper;
import sk.tuke.mp.core.sql.Field;
import sk.tuke.mp.core.sql.FieldTypes;
import sk.tuke.mp.core.sql.Table;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static sk.tuke.mp.utils.ReflectionExtension.generateSetterName;

public class Get implements Query {

    private Table table;
    private Class type;
    private PersistenceMapper<Table> mapper;
    private Integer id;
    private HashMap<String, Object> criteria = new HashMap();

    public Get(Class type, PersistenceMapper<Table> mapper, Object... args) {
        this.type = type;
        this.table = mapper.getUnit(type);
        this.mapper = mapper;
        if(args.length == 1) {
            criteria.put(table.getPrimaryKeyField().getName(), args[0]);
        } else {
            for(int i = 0; i < args.length; i += 2) {
                if(i + 1 == args.length) return;
                criteria.put((String)args[i], args[i + 1]);
            }
        }
    }

    private String getQuery(Connection conn) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * ")
        .append(" FROM ")
        .append(this.table.getName());
        if(criteria.size() > 0) {
            sb.append(" WHERE ");
            List<String> crits = new ArrayList<>();
            for (Map.Entry crit : criteria.entrySet()) {
                crits.add(crit.getKey() + "=" + FieldTypes.escapeValue(crit.getValue()));
            }
            sb.append(String.join(" AND ", crits));
        }
        sb.append(";");
        return sb.toString();
    }

    @Override
    public List<Object> executeQuery(Connection conn) throws SQLException {
        String cmd = getQuery(conn);
        ResultSet rs = conn.createStatement().executeQuery(cmd);
        List<Object> list = new ArrayList<Object>();
        while (rs.next()) {
            try {
                Object obj = type.newInstance();
                List<Field> fields = mapper.getUnit(type).getFields();
                for(Field f: fields) {
                    java.lang.reflect.Field reflectField = type.getDeclaredField(f.getOriginalName());
                    Method setter = null;
                    String setterName = generateSetterName(f.getOriginalName());
                    setter = type.getMethod(setterName, reflectField.getType());
                    if(f.getName().toLowerCase().equals(f.getOriginalName().toLowerCase()))
                        setter.invoke(obj, rs.getObject(f.getName()));
                    else {
                        Object foreignKeyValue =  rs.getObject(f.getName());
                        Class foreignType = reflectField.getType();
                        // Table foreignTable = mapper.getUnit(foreignType);
                        List<Object> foreignObjects = new Get(foreignType, mapper, foreignKeyValue).executeQuery(conn);
                        if(foreignObjects.size() > 0)
                            setter.invoke(obj, foreignObjects.get(0));
                        else
                            // throw new SQLException("Non existing entry in " + foreignTable.getName() + " foreign table for field " + f.getName() + "=" + foreignKeyValue.toString() + ".");
                            setter.invoke(obj,null);
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
