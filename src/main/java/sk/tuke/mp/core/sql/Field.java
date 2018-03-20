package sk.tuke.mp.core.sql;

import sk.tuke.mp.core.sql.decorators.AdditionalDecorator;
import sk.tuke.mp.utils.Semantics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Field {

    private String name;

    private String originalName;

    private FieldTypes type;

    private List<String> arguments = new ArrayList<>();

    private Table table;

    private List<Field> decorators = new ArrayList<>();


    public Field() {}

    protected Field(Field f) {
        name = f.getName();
        originalName = f.getOriginalName();
        type = f.getType();
        arguments = f.getArguments();
        decorators.addAll(f.decorators);
        decorators.add(this);
    }

    public static Field create(String name, FieldTypes type) {
        Field f = new Field();
        f.setName(name);
        f.setType(type);
        f.setArguments(new ArrayList<>());
        return f;
    }

    @Override
    public String toString() {
        return String.format("%c%s%c %s%s %s", '"', name, '"', type.toString(), formatArguments(), formatDecorators());
    }

    public String formatArguments() {
        return arguments.size() > 0 ? '(' + String.join(", ", arguments) + ')' : "";
    }

    public String formatDecorators() {
        StringBuilder decoratorsSb = new StringBuilder();
        for(Field decorator : decorators) {
            if(!(decorator instanceof AdditionalDecorator)) {
                decoratorsSb.append(decorator.toStringDecoration())
                        .append(Constants.WHITESPACE);
            }
        }
        return decoratorsSb.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(FieldTypes type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public FieldTypes getType() {
        return type;
    }

    public String getOriginalName() {return originalName; }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        if(arguments.size() + this.arguments.size() == 0) {
            this.arguments = this.type.getDefaultArguments();
        } else {
            this.arguments = arguments;
        }
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<Field> getDecorators() {
        return decorators;
    }

    public String toStringDecoration() { return ""; }

    public static List<String> getFieldNames(List<Field> fields) {
        List<String> result = new ArrayList<>();
        for(Field field : fields) {
            result.add(field.getName());
        }

        return result;
    }
}
