package sk.tuke.mp.core;

import sk.tuke.mp.core.sql.Table;

import javax.persistence.PersistenceException;
import java.util.Set;

public interface PersistenceMapper<T> {

    T mapEntityClass(Class entity) throws PersistenceException;

    Set<T> getUnits();
    Set<Class> getEntities();

    T getUnit(Class cls);
    Class getEntity(T unit);

    T findUnitByName(String name);

}
