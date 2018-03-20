package sk.tuke.mp.utils;

import sk.tuke.mp.core.sql.Field;
import sk.tuke.mp.core.sql.commands.SQLDialects;

public class Semantics {

    public static String inSingleQuotes(String str) {
        return "'" + str + "'";
    }

    public static String inDoubleQuotes(String str) {
        return "\"" + str + "\"";
    }

    public static String inCBracesQuotes(String str) {
        return "[" + str + "]";
    }

    public static String inBackTickQuotes(String str) {
        return "`" + str + "`";
    }

    public static String getLastToken(String delRegex, String str) {
        String[] splitted = str.split(delRegex);
        return splitted[splitted.length - 1];
    }

    public static String getLastToken(String str) {
        return getLastToken("\\.", str);
    }

    public static String escapeReference(String ref, SQLDialects dialect) {
        switch (dialect) {
            case DERBY: return ref;
            case MSSQL: case MSACCESS: return inCBracesQuotes(ref);
            case MYSQL: return inBackTickQuotes(ref);
            default: return inDoubleQuotes(ref);
        }
    }

    public static String formatField(Field field, SQLDialects dialect) {
        return String.format("%s %s%s %s", escapeReference(field.getName(),dialect), field.getType().toString(), field.formatArguments(), field.formatDecorators());
    }

    public static String getClosingChar(SQLDialects dialect) {
        return dialect == SQLDialects.DERBY ? "" : ";";
    }
}
