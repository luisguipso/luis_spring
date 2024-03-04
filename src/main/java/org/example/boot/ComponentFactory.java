package org.example.boot;

import org.example.datastructures.ComponentsInstances;
import org.example.datastructures.ServiceImplementationMap;
import org.example.util.ClassUtil;
import org.example.util.LuisLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;

public class ComponentFactory {

    public static final int DEFAULT_CONSTRUCTOR_PARAMETERS_NUMBER = 0;
    public static final int DEFAULT_NUMBER_OF_CONSTRUCTORS = 1;

    public static Object createComponent(String className) {
        LuisLogger.log(ComponentFactory.class, "Creating new component instance, for: " + className);
        try {
            return createComponentInstance(ClassUtil.getClass(className));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object createComponentInstance(Class<?> componentClass) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?>[] constructors = componentClass.getConstructors();
        Optional<Object> component = Optional.empty();
        for (Constructor<?> constructor : constructors)
            component = of(invokeConstructor(constructor, constructors.length));

        return component.orElseThrow();
    }

    private static Object invokeConstructor(Constructor<?> constructor, int constructorsCount) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (onlyDefautConstructorIsPresent(constructor, constructorsCount))
            return constructor.newInstance();

        List<String> parametersTypes = getParametersTypes(constructor);
        List<Object> parameters = getParametersInstances(parametersTypes);
        return constructor.newInstance(parameters.toArray());
    }

    private static boolean onlyDefautConstructorIsPresent(Constructor<?> constructor, int constructorsCount) {
        return constructor.getParameterCount() == DEFAULT_CONSTRUCTOR_PARAMETERS_NUMBER
                && constructorsCount == DEFAULT_NUMBER_OF_CONSTRUCTORS;
    }

    private static List<String> getParametersTypes(Constructor<?> constructor) {
        return getRawParamTypes(constructor).stream()
                .map(ComponentFactory::replaceInterfaceByImplementation)
                .toList();
    }

    private static List<String> getRawParamTypes(Constructor<?> constructor) {
        List<String> paramClassNames = Arrays.stream(constructor.getParameterTypes()).map(Class::getName).toList();
        return new ArrayList<>(paramClassNames);
    }

    private static String replaceInterfaceByImplementation(String type) {
        String implementationType = ServiceImplementationMap.implementations.get(type);
        if (!ComponentUtils.isALuisComponent(type) && implementationType != null)
            return implementationType;
        return type;
    }

    private static List<Object> getParametersInstances(List<String> parametersTypes) {
        if (!parametersTypes.stream().allMatch(ComponentUtils::isALuisComponent)) {
            String parameter = getParameterIsNotAComponent(parametersTypes);
            throw new RuntimeException(String.format("Parameter: %s is not a Component", parameter));
        }

        return instantiateParameters(parametersTypes);
    }

    private static String getParameterIsNotAComponent(List<String> parametersTypes) {
        return parametersTypes.stream()
                .filter(each -> !ComponentUtils.isALuisComponent(each))
                .findFirst()
                .orElseThrow();
    }

    private static List<Object> instantiateParameters(List<String> parameterClassNames) {
        return parameterClassNames.stream()
                .map(each -> {
                    Object p = ComponentsInstances.instances.get(each);
                    if (p == null)
                        p = createComponent(each);
                    return p;
                })
                .toList();
    }

}
