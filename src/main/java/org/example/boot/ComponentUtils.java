package org.example.boot;

import org.example.annotation.LuisComponent;
import org.example.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public final class ComponentUtils {
    private ComponentUtils() {}

    public static boolean isALuisComponent(String className) {
        Class<?> clazz = ClassUtil.getClass(className);
        return Arrays.stream(clazz.getAnnotations())
                .map(Annotation::annotationType)
                .anyMatch(ComponentUtils::isLuisComponentAnnotation);
    }

    private static boolean isLuisComponentAnnotation(Class<? extends Annotation> annotationType) {
        return annotationType.equals(LuisComponent.class)
                || Arrays.stream(annotationType.getAnnotations())
                .anyMatch(each -> each.annotationType().equals(LuisComponent.class));
    }
}
