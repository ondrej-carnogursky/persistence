package sk.tuke.mp.persistence;

import sk.tuke.mp.core.PersistenceMapper;
import sk.tuke.mp.core.sql.PersistenceMapperSQL;
import sk.tuke.mp.core.sql.Table;
import sk.tuke.mp.core.sql.commands.*;

import javax.persistence.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


// for now only SQL: MYSQL, SQLITE, POSTGRES, MSSQL, ORACLE, MSACCESS
// TODO: support NoSQL also (MongoDB, Cassandra, Neo4J, HBase, ...)
public class ReflectivePersistenceManager implements PersistenceManager {

    private Connection conn;
    private PersistenceMapper<Table> mapper;


    public ReflectivePersistenceManager(Connection databaseConnection) {
        this.conn = databaseConnection;
        this.mapper = new PersistenceMapperSQL();
    }

    @Override
    public void createTables(Class... classes) throws PersistenceException {
        for(Class cls : classes) {
            this.mapper.mapEntityClass(cls);
        }

        try {
            for(Table table : mapper.getUnits()) {
                new CreateTable(table, SQLDialects.detect(conn))
                        .execute(conn);
            }
        } catch(SQLException e) {
            throw new PersistenceException("Something went wrong, see: \"" + e.getMessage() + "\"");
        }
    }

    @Override
    public <T> List<T> getAll(Class<T> type) throws PersistenceException {
        this.mapper.mapEntityClass(type);
        try {
            return (List<T>)new Get(type, mapper).executeQuery(conn);
        } catch(SQLException e) {
            throw new PersistenceException("Something went wrong, see: \"" + e.getMessage() + "\"");
        }
    }

    @Override
    public <T> T get(Class<T> type, int id) throws PersistenceException {
        this.mapper.mapEntityClass(type);
        try {
            List<T> result = (List<T>)new Get(type, mapper, id).executeQuery(conn);
            if(result.size() > 0)
                return (T)(result.get(0));
            else
                return null;
        } catch(SQLException e) {
            throw new PersistenceException("Something went wrong, see: \"" + e.getMessage() + "\"");
        }
    }

    @Override
    public <T> List<T> getBy(Class<T> type, String fieldName, Object value) {
        this.mapper.mapEntityClass(type);
        try {
            return (List<T>)new Get(type, mapper, fieldName, value).executeQuery(conn);
        } catch(SQLException e) {
            throw new PersistenceException("Something went wrong, see: \"" + e.getMessage() + "\"");
        }
    }

    @Override
    public int save(Object value) throws PersistenceException {
        try {
            return new InsertOrUpdate(value, mapper).execute(conn);
        } catch(Exception e) {
            throw new PersistenceException("Something went wrong, see: \"" + e.getMessage() + "\"");
        }
    }
}
