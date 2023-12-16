package org.example.boot;

import org.example.annotation.LuisComponent;
import org.example.datastructures.ComponentsInstances;
import org.example.datastructures.ServiceImplementationMap;
import org.example.util.ClassUtil;
import org.example.util.LuisLogger;
import org.example.web.DispatchServlet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;

public class ComponentLoader {

    public void loadComponents(List<String> allClasses) {
        allClasses.stream()
                .filter(ComponentLoader::isALuisComponent)
                .forEach(className -> {
                    Object component = loadComponent(className);
                    ComponentsInstances.instances.put(className, component);
                });
    }

    public static Object loadComponent(String className) {
        LuisLogger.log(DispatchServlet.class, "Creating new component instance, for: " + className);
        try {
            return invokeComponentConstructors(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object invokeComponentConstructors(String className) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?>[] constructors = ClassUtil.getClass(className).getConstructors();
        Optional<Object> component = Optional.empty();
        for (Constructor<?> constructor : constructors)
            component = of(invokeConstructor(className, constructors, constructor));

        return component.orElseThrow();
    }

    private static Object invokeConstructor(String className, Constructor<?>[] constructors, Constructor<?> constructor) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (constructor.getParameterCount() == 0 && constructors.length == 1)
            return constructor.newInstance();

        List<String> parametersTypes = getParametersTypes(constructor);
        List<Object> instantiatedParameters = getParametersInstances(parametersTypes, className);


        Class<?>[] paramClasses = new Class<?>[parametersTypes.size()];
        Constructor<?> c = null;
        try {
            Class<?> apsClass = Class.forName(className);
            for (int i = 0; i < parametersTypes.size(); i++) {
                paramClasses[i] = Class.forName(parametersTypes.get(i));
            }

            c = apsClass.getConstructor(paramClasses);
        } catch (Exception e){
            e.printStackTrace();
        }
        return c.newInstance(instantiatedParameters.toArray());

    }

    private static List<String> getParametersTypes(Constructor<?> constructor) {
        List<String> parametersTypes = new ArrayList<>(Arrays.stream(constructor.getParameterTypes()).map(Class::getName).toList());
        for (String type : parametersTypes) {
            String implementationType = ServiceImplementationMap.implementations.get(type);
            if (!isALuisComponent(type) && implementationType != null)
                parametersTypes.set(parametersTypes.indexOf(type), implementationType);
        }
        return parametersTypes;
    }

    private static List<Object> getParametersInstances(List<String> parametersTypes, String className) {
        if (!parametersTypes.stream().allMatch(ComponentLoader::isALuisComponent)) {
            String notAComponent = getParameterIsNotAComponent(parametersTypes);
            throw new RuntimeException(String.format("Could not instantiate all parameters for: %s. Parameter: %s is not a Component",
                    className,
                    notAComponent));
        }

        return instantiateParameters(parametersTypes);
    }

    private static String getParameterIsNotAComponent(List<String> parametersTypes) {
        return parametersTypes.stream()
                .filter(each -> !isALuisComponent(each))
                .findFirst()
                .orElseThrow();
    }

    private static List<Object> instantiateParameters(List<String> parameterClassNames) {
        return parameterClassNames.stream()
                .map(each -> {
                    Object p = ComponentsInstances.instances.get(each);
                    if (p == null)
                        p = loadComponent(each);
                    return p;
                })
                .toList();
    }

    private static boolean isALuisComponent(String className) {
        Class<?> clazz = ClassUtil.getClass(className);
        return Arrays.stream(clazz.getAnnotations())
                .map(Annotation::annotationType)
                .anyMatch(ComponentLoader::isLuisComponentAnnotation);
    }

    private static boolean isLuisComponentAnnotation(Class<? extends Annotation> annotationType) {
        return Arrays.stream(annotationType.getAnnotations())
                .anyMatch(each -> each.annotationType().equals(LuisComponent.class));
    }
}
