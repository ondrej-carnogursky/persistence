package sk.tuke.mp.core.sql.decorators;

import sk.tuke.mp.core.sql.Constants;
import sk.tuke.mp.core.sql.Field;
import sk.tuke.mp.core.sql.Table;
import sk.tuke.mp.core.sql.commands.SQLDialects;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForeignKeyTable extends Table implements DialectDependentDecorator, AdditionalDecorator  {

    private List<Field> myFields = new ArrayList<>();

    private Map<Table, List<Field>> foreignFields = new HashMap<>();

    private String foreignKeyName;

    public ForeignKeyTable(Table table, Field field, Field foreignField) {
        super(table);
        if(table instanceof ForeignKeyTable) {
            this.foreignFields = ((ForeignKeyTable) table).getForeignFields();
            this.myFields = ((ForeignKeyTable) table).getMyFields();
        }
        addForeignField(foreignField);
        this.myFields.add(field);
    }

    public ForeignKeyTable(Field field, Field foreignField) {
        this(field.getTable(), field, foreignField);
    }

    public String toStringDecoration(SQLDialects dialect) {

        List<String> result = new ArrayList<>();
        for (Map.Entry<Table, List<Field>> foreignEntry : foreignFields.entrySet()) {
            String myColumns = String.join(", ", Field.getFieldNames(myFields));
            String foreignColumns = String.join(", ", Field.getFieldNames(foreignEntry.getValue()));
            if(dialect == SQLDialects.POSTGRES) {
                result.add(String.format("FOREIGN KEY (%s) REFERENCES %s (%s)\n", myColumns, foreignEntry.getKey().getName(), foreignColumns));
            } else {
                foreignKeyName = String.format("FK_%s", getName() + foreignEntry.getKey().getName());
                result.add(String.format("CONSTRAINT %s FOREIGN KEY (%s)\n" +
                        "REFERENCES %s(%s)", foreignKeyName, myColumns, foreignEntry.getKey().getName(), foreignColumns));
            }
        }
        return String.join("," + Constants.NEWLINE, result) + Constants.NEWLINE;
    }

    public List<Field> getMyFields() {
        return myFields;
    }

    public void setMyFields(List<Field> myFields) {
        this.myFields = myFields;
    }

    public Map<Table, List<Field>> getForeignFields() {
        return foreignFields;
    }

    public boolean isForeignKey(Field field) {
        return myFields.contains(field);
    }

    public Field getForeignField(Field myField) {
        Table foreignTable = getForeignFields().keySet().stream()
                .filter(t -> t.getName().toLowerCase().equals(myField.getOriginalName().toLowerCase()))
                .findFirst().get();
        return getForeignFields().get(foreignTable).get(0);
    }

    public void addForeignField(Field foreignField) {
        List<Field> foreigns = this.foreignFields.getOrDefault(foreignField.getTable(), null);
        if(foreigns == null) {
            foreigns = new ArrayList<>();
            this.foreignFields.put(foreignField.getTable(), foreigns);
        }
        foreigns.add(foreignField);
    }


    public void propagate(Object obj) {

    }
}
