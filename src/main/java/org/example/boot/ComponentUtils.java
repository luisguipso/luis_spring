package org.example.boot;

import org.example.annotation.LuisComponent;
import org.example.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class ComponentUtils {
    public static boolean isALuisComponent(String className) {
        Class<?> clazz = ClassUtil.getClass(className);
        return Arrays.stream(clazz.getAnnotations())
                .map(Annotation::annotationType)
                .anyMatch(ComponentUtils::isLuisComponentAnnotation);
    }

    private static boolean isLuisComponentAnnotation(Class<? extends Annotation> annotationType) {
        return Arrays.stream(annotationType.getAnnotations())
                .anyMatch(each -> each.annotationType().equals(LuisComponent.class));
    }
}
