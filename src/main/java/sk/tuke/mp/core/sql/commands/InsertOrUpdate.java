package sk.tuke.mp.core.sql.commands;

import sk.tuke.mp.core.PersistenceMapper;
import sk.tuke.mp.core.sql.Table;
import sk.tuke.mp.persistence.Cache;

import java.sql.Connection;
import java.sql.SQLException;

public class InsertOrUpdate implements Command {

    private Table table;
    private Object object;
    private PersistenceMapper<Table> mapper;
    private Cache cache;

    public InsertOrUpdate(Object object, PersistenceMapper<Table> mapper, Cache cache) {
        this.object = object;
        this.table = mapper.getUnit(object.getClass());
        this.mapper = mapper;
        this.cache = cache;
    }

    @Override
    public int execute(Connection conn) throws SQLException {
        cache.tryPutInstance(object);
        int affectedRows;
        try {
            affectedRows = new Update(object, mapper, cache).execute(conn);
            if(affectedRows > 0)
                return affectedRows;
            else
                throw new SQLException(new Throwable("Updating non existing row."));
        } catch(SQLException e) {
            affectedRows = new Insert(object, mapper, cache).execute(conn);
            if (affectedRows > 0)
                return affectedRows;
            else
                throw new SQLException(new Throwable("Inserting new row not succesfull."));
        }
    }
}
