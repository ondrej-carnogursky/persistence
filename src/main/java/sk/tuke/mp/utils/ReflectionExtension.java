package sk.tuke.mp.utils;

import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionExtension {

    public static Object getPropertyValue(Object obj, Field field) {
        try {
            if(field.isAccessible()) {
                return field.get(obj);
            } else {
                Method getter = getGetter(obj, field);
                if(getter != null)
                    return getter.invoke(obj);
                else {
                    field.setAccessible(true);
                    Object result = field.get(obj);
                    field.setAccessible(false);
                    return result;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getPropertyValue(Object obj, String name) {
        try {
            Class<?> cls = obj.getClass();
            Field field = cls.getDeclaredField(name); //getField(name);
            return getPropertyValue(obj, field);
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateGetterName(String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String generateSetterName(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static void setPropertyValue(Object obj, Field field, Object value) {
        try {
            // field.set(obj, value);
            if(field.isAccessible()) {
                field.set(obj, value);
            } else {
                Method setter = getSetter(obj, field);
                if(setter != null)
                    setter.invoke(obj, value);
                else {
                    field.setAccessible(true);
                    field.set(obj, value);
                    field.setAccessible(false);
                }
            }
        } catch (Exception e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public static void setPropertyValue(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            setPropertyValue(obj, field, value);
        } catch (Exception e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public static Method getSetter(Object obj, Field field) {
        try {
            String setterName = generateSetterName(field.getName());
            return obj.getClass().getMethod(setterName, field.getType());
        } catch(Exception e) {
            return null;
        }
    }

    public static Method getGetter(Object obj, Field field) {
        try {
            String getterName = generateGetterName(field.getName());
            Method getter = obj.getClass().getMethod(getterName);
            if(getter != null && getter.getReturnType() == field.getType() && getter.getParameterCount() == 0)
                return getter;
            else
                return null;
        } catch(Exception e) {
            return null;
        }
    }

    public static boolean isFieldAccessible(Object obj, Field field) {
        return field.isAccessible() || getSetter(obj, field) != null;
    }

}
