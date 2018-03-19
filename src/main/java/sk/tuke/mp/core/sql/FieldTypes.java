package sk.tuke.mp.core.sql;

import java.util.ArrayList;
import java.util.List;

public enum FieldTypes {

    VARCHAR, INT, DOUBLE, UNKNOWN; // TODO: add others...


    public static FieldTypes mapJavaToSQL(Object type) {
        return type instanceof Double ? DOUBLE
                : type instanceof Integer ? INT
                : type instanceof String ? VARCHAR
                // TODO: add others...
                : UNKNOWN;
    }

    public static FieldTypes mapJavaToSQL(Class type) {
        return type == Double.class || type.getName() == "double" ? DOUBLE
                : type == Integer.class || type.getName() == "int" ? INT
                : type == String.class ? VARCHAR
                // TODO: add others...
                : UNKNOWN;
    }

    @Override
    public String toString() {
        switch(this) {
            case INT: return "int";
            case DOUBLE: return "double";
            case VARCHAR: return "varchar";
            default: return "";
        }
    }

    public static String escapeValue(Object value) {
        if(value instanceof String) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }

    public List<String> getDefaultArguments() {
        List<String> arguments = new ArrayList<>();
        if(this == FieldTypes.VARCHAR || this == FieldTypes.DOUBLE) {
            arguments.add("255");
        }
        return arguments;
    }
}
