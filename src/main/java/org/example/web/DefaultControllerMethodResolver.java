package org.example.web;

import org.example.annotation.LuisRequestParam;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class DefaultControllerMethodResolver implements ControllerMethodResolver{
    @Override
    public List<Method> getControllerMethods(Object controller, String controllerMethod, Map<String, String[]> requestParameterMap) {
        Method[] methods = controller.getClass().getMethods();

        Predicate<Method> predicate = getMethodPredicate(controllerMethod, requestParameterMap);
        return Arrays.stream(methods)
                .filter(predicate)
                .toList();
    }

    private static Predicate<Method> getMethodPredicate(String controllerMethod, Map<String, String[]> parameterMap) {
        Predicate<Method> predicate = each -> each.getName().equals(controllerMethod);

        Set<String> requestParamNames = parameterMap.keySet();
        int numberOfRequestParams = parameterMap.size();
        if (numberOfRequestParams > 0)
            predicate = predicate.and(method -> requestParamNames.containsAll(getMethodRequestParamNames(method))
                                                && numberOfRequestParams == method.getParameterCount());

        return predicate;
    }

    private static List<String> getMethodRequestParamNames(Method method) {
        return Arrays.stream(method.getParameters())
                .flatMap(p -> Arrays.stream(p.getAnnotations())
                        .filter(LuisRequestParam.class::isInstance)
                        .map(lrp -> ((LuisRequestParam) lrp).value()))
                .toList();
    }
}
