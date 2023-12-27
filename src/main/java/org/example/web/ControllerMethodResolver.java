package org.example.web;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface ControllerMethodResolver {
    List<Method> getControllerMethods(Object controller, String controllerMethod, Map<String, String[]> requestParameterMap);
}
