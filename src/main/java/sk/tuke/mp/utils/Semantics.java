package sk.tuke.mp.utils;

public class Semantics {

    public static String inDoubleQuotes(String str) {
        return "\"" + str + "\"";
    }

    public static String inCBracesQuotes(String str) {
        return "\"" + str + "\"";
    }

    public static String inBackTickQuotes(String str) {
        return "\"" + str + "\"";
    }

    public static String getLastToken(String delRegex, String str) {
        String[] splitted = str.split(delRegex);
        return splitted[splitted.length - 1];
    }

    public static String getLastToken(String str) {
        return getLastToken("\\.", str);
    }
}
