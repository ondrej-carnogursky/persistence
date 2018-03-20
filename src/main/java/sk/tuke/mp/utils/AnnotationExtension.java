package sk.tuke.mp.utils;

import javax.persistence.Id;
import javax.persistence.IdClass;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class AnnotationExtension {

    public static Field getIdField(Object obj) {
        Class cls = obj.getClass();
        Optional result = Arrays.stream(cls.getDeclaredFields())
        .filter(f -> f.getAnnotation(Id.class) != null)
        .findFirst();
        return result.isPresent() ? (Field)result.get() : null;
    }

    public static Object getIdValue(Object obj) {
        Class cls = obj.getClass();
        Field idField = getIdField(obj);
        return idField == null ? null : ReflectionExtension.getPropertyValue(obj, idField.getName());
    }
}
