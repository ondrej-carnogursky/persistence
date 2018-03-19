package sk.tuke.mp.core.sql;

import sk.tuke.mp.core.sql.decorators.PrimaryKeyField;

import java.util.ArrayList;
import java.util.List;

public class Table {

    private List<Field> fields;
    private String name;

    public Table() {}

    public Table(Table table) {
        this();
        this.name = table.getName();
        this.fields = table.getFields();
    }


    public static Table create(String name, List<Field> fields) {
        Table t = new Table();
        t.setFields(fields);
        return t;
    }

    public static Table create(String name) {
        Table t = new Table();
        t.setName(name);
        t.setFields(new ArrayList<>());
        return t;
    }

    public List<Field> getFields() {
        return fields;
    }


    public void setFields(List<Field> fields) {
        this.fields = fields;
        for(Field field : this.fields) {
            field.setTable(this);
        }
    }


    public void addField(Field field) {
        this.fields.add(field);
        field.setTable(this);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PrimaryKeyField getPrimaryKeyField() {
        for(Field field : fields) {
            if(field instanceof PrimaryKeyField) {
                return (PrimaryKeyField) field;
            }
        }
        return null;
    }
}
