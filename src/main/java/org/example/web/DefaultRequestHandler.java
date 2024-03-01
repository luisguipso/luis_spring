package org.example.web;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;

import java.lang.reflect.Method;
import java.util.Optional;
import org.example.web.exception.MethodNotFoundException;
import org.example.web.parameter.MethodParameterResolver;

public class DefaultRequestHandler implements RequestHandler{

    private final ControllerDataResolver controllerDataResolver;
    private final ControllerInstanceResolver controllerInstanceResolver;
    private final MethodParameterResolver methodParameterResolver;

    public DefaultRequestHandler(ControllerDataResolver controllerDataResolver,
                                 MethodParameterResolver methodParameterResolver, ControllerInstanceResolver controllerInstanceResolver) {
        this.controllerDataResolver = controllerDataResolver;
        this.controllerInstanceResolver = controllerInstanceResolver;
        this.methodParameterResolver = methodParameterResolver;
    }

    @Override
    public Object handleRequest(HttpServletRequest request) throws InvocationTargetException, IllegalAccessException {
        RequestControllerData data = getRequestControllerData(request);
        Object controller = controllerInstanceResolver.getController(data.getControllerClass());
        Method controllerMethod = data.getMethod();
        Object[] args = methodParameterResolver.resolveMethodParameters(request, controllerMethod, data.getUrl());
        logMethodInformation(controllerMethod);
        return controllerMethod.invoke(controller, args);
    }

    private RequestControllerData getRequestControllerData(HttpServletRequest request) {
        Optional<RequestControllerData> foundData = controllerDataResolver.findController(request);
        if (foundData.isEmpty())
            throw new MethodNotFoundException("Controller not found");

        return foundData.get();
    }

    private void logMethodInformation(Method controllerMethod) {
        LuisLogger.log(getClass(), "Invoking method " + controllerMethod.getName() + " to handle request");
        if (controllerMethod.getParameterCount() > 0)
            LuisLogger.log(getClass(), "Method " + controllerMethod.getName() + " has parameters");
    }
}
