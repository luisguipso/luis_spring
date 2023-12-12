package org.example.boot;

import org.example.annotation.LuisComponent;
import org.example.datastructures.ComponentsInstances;
import org.example.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public class ComponentLoader {

    public void loadComponents(List<String> allClasses) {
        allClasses.stream()
                .filter(ComponentLoader::isALuisComponent)
                .forEach(className -> {
                    Object component = ComponentFactory.getComponent(className);
                    ComponentsInstances.instances.put(className, component);
                });
    }

    private static boolean isALuisComponent(String className) {
        Class<?> clazz = ClassUtil.getClass(className);
        return Arrays.stream(clazz.getAnnotations())
                .anyMatch(each -> isLuisComponentAnnotation(each.annotationType()));
    }

    private static boolean isLuisComponentAnnotation(Class<? extends Annotation> annotationType) {
        return Arrays.stream(annotationType.getAnnotations())
                .anyMatch(each -> each.annotationType().equals(LuisComponent.class));
    }
}
