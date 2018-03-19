package sk.tuke.mp.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionExtension {

    public static Object getPropertyValue(Object obj, String name) {
        try {
            Class<?> cls = obj.getClass();
            Field field = cls.getDeclaredField(name); //getField(name);

            if(field.isAccessible()) {
                return field.get(obj);
            }

            Method getter = cls.getMethod(generateGetterName(name));

            //if(getter.isAccessible() && getter.getReturnType() == cls && getter.getParameterCount() == 0) {
            if(getter != null && getter.getReturnType() == field.getType() && getter.getParameterCount() == 0) {
                return getter.invoke(obj);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public static String generateGetterName(String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String generateSetterName(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
