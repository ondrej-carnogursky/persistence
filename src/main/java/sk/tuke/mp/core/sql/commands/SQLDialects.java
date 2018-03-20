package sk.tuke.mp.core.sql.commands;

import java.sql.Connection;

public enum SQLDialects {

    MYSQL, SQLITE, POSTGRES, DERBY, // Community
    MSSQL, ORACLE, MSACCESS, // Corporate
    UNKNOWN;


    public static SQLDialects detect(Connection connection) {
        String connectionClsName = connection.getClass().getName().toLowerCase();
        return connectionClsName.contains("mysql") ? MYSQL
                : connectionClsName.contains("sqlite") ? SQLITE
                : connectionClsName.contains("postgre") ? POSTGRES
                : connectionClsName.contains("microsoft") && connectionClsName.contains("sqlserver")  ? MSSQL
                : connectionClsName.contains("oracle")  ? ORACLE
                : connectionClsName.contains("microsoft") && connectionClsName.contains("access")  ? MSACCESS
                : connectionClsName.contains("derby")  ? DERBY
                : UNKNOWN;
    }
}
