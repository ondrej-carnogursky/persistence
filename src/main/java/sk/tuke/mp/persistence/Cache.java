package sk.tuke.mp.persistence;

import sk.tuke.mp.utils.AnnotationExtension;
import sk.tuke.mp.utils.ReflectionExtension;

import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Cache {
    private HashMap<Class, HashMap<Object, Object>> cache = new HashMap<>();

    public Object getInstance(Object obj) {
        Class cls = obj.getClass();
        if(cache.containsKey(cls)) {
            Object idValue = AnnotationExtension.getIdValue(obj);
            if (cache.get(cls).containsKey(idValue))
                return cache.get(cls).get(idValue);
        }
        return null;
    }

    public void tryPutInstance(Object obj) {
        // check if exists
        Object cachedObj = getInstance(obj);
        if(cachedObj != null) {
            if(cachedObj == obj)
                return;
            // another object with same idField value, start autoincrementation...
            // check idField accessibility (public member or setter is present)
            Field idField = AnnotationExtension.getIdField(obj);
            if(!ReflectionExtension.isFieldAccessible(obj, idField)) {
                // check if is incrementable
                if(Number.class.isAssignableFrom(idField.getType())) {
                    throw new PersistenceException("Cannot autoincrement idField " + idField.getName() + ", because it's not numeric.");
                } else {
                    // perform autoincrement on idField
                    Object maxIdValue = cache.get(obj.getClass()).keySet().stream()
                            .collect(Collectors.maxBy((o1,o2) -> (Integer)o1 - (Integer)o2)).get();
                    ReflectionExtension.setPropertyValue(obj, idField, (Integer)maxIdValue + 1);
                }
            } else {
                // idField is accessible, so user need to set right idValue
                Object idValue = ReflectionExtension.getPropertyValue(obj, idField);
                throw new PersistenceException("There is another object with the same " + idField.getName() + " value of " + idValue.toString() + " in DB cache.");
            }
        }
        // now put
        Class cls = obj.getClass();
        if(!cache.containsKey(cls))
            cache.put(cls, new HashMap<>());
        cache.get(cls).put(AnnotationExtension.getIdValue(obj), obj);
    }

    public Object getById(Class cls, Object idValue) {
        if(cache.containsKey(cls)) {
            if (cache.get(cls).containsKey(idValue))
                return cache.get(cls).get(idValue);
        }
        return null;
    }
}
