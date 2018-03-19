package sk.tuke.mp.core.sql;


import sk.tuke.mp.core.PersistenceMapper;
import sk.tuke.mp.core.sql.decorators.ForeignKeyTable;
import sk.tuke.mp.core.sql.decorators.PrimaryKeyField;
import sk.tuke.mp.utils.Semantics;

import javax.persistence.*;
import java.util.*;


// Unit = SQL Table
// Entity = Class<?>
public class PersistenceMapperSQL implements PersistenceMapper<Table> {

    private Map<Table, Class> mappings = new LinkedHashMap<>();
    private Map<Class, Table> mappingsBack = new LinkedHashMap<>();

    public Table mapEntityClass(Class entity) throws PersistenceException {

        Table potentialExsisting = findUnitByEntityName(entity.getName());
        if(potentialExsisting != null) {
            return potentialExsisting;
        }

        if(!entity.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Entity annotation is not present");
        }
        Table table = Table.create(Semantics.getLastToken(entity.getName()));

        for(java.lang.reflect.Field field : entity.getDeclaredFields()) {
            if(field.getAnnotation(Transient.class) == null) {
                Field sqlField = Field.create(field.getName(), FieldTypes.mapJavaToSQL(field.getType()));
                sqlField = applyDecorators(field, sqlField, table);
                table.addField(sqlField);


                if(field.getAnnotation(ManyToOne.class) != null) {
                    Table foreign = getUnit(findEntityByName(field.getType().getName()));
                    if(foreign == null) {
                        foreign = mapEntityClass(field.getType());
                    }
                    PrimaryKeyField foreignPrimary = foreign.getPrimaryKeyField();
                    sqlField.setOriginalName(sqlField.getOriginalName());
                    sqlField.setName(sqlField.getName() + "ID");
                    sqlField.setType(foreignPrimary.getType());
                    sqlField.setArguments(foreignPrimary.getArguments());
                    table = new ForeignKeyTable(table, sqlField, foreignPrimary);
                }
            }
        }

        put(table, entity);
        return table;
    }

    public Set<Table> getUnits() {
        return mappings.keySet();
    }

    public Set<Class> getEntities() {
        return mappingsBack.keySet();
    }

    public Table findUnitByName(String name) {
        for(Table table : getUnits()) {
            if(table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }

    public Class findEntityByName(String name) {
        for(Class entity : getEntities()) {
            if(entity.getName().equals(name)) {
                return entity;
            }
        }
        return null;
    }

    public Class findEntityByUnitName(String name) {
        return getEntity(findUnitByName(name));
    }

    public Table findUnitByEntityName(String name) {
        return getUnit(findEntityByName(name));
    }

    @Override
    public Table getUnit(Class cls) {
        return mappingsBack.get(cls);
    }

    @Override
    public Class getEntity(Table unit) {
        return mappings.get(unit);
    }


    private void put(Table t, Class c) {
        mappings.put(t, c);
        mappingsBack.put(c, t);
    }

    private Field applyDecorators(java.lang.reflect.Field field, Field sqlField, Table table) {
        // TODO: supported annotations + interfering annotations
        if(field.getAnnotation(Id.class) != null) {
            sqlField = new PrimaryKeyField(sqlField);
        }
        return sqlField;
    }
}
