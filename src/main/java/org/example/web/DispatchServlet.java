package org.example.web;


import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.datastructures.ComponentsInstances;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DispatchServlet extends HttpServlet {

    private final transient Gson gson;
    private final ControllerResolver controllerResolver;
    private final ControllerMethodResolver controllerMethodResolver;
    private final MethodParameterResolver methodParameterResolver;

    public DispatchServlet(ControllerResolver controllerResolver,
                           ControllerMethodResolver controllerMethodResolver,
                           MethodParameterResolver methodParameterResolver) {
        this.gson = new Gson();
        this.controllerResolver = controllerResolver;
        this.controllerMethodResolver = controllerMethodResolver;
        this.methodParameterResolver = methodParameterResolver;
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURL().toString().endsWith("/favicon.ico"))
            return;

        Optional<RequestControllerData> foundData = controllerResolver.findController(request);
        if (foundData.isEmpty()) {
            response.sendError(404, "Not Found.");
            return;
        }
        RequestControllerData data = foundData.get();

        try {
            Object result = getResultFromControllerMethod(request, data);
            writeResponse(result, response.getWriter()).close();
        } catch (IOException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            response.sendError(500, e.getMessage());
        }
    }

    Object getResultFromControllerMethod(HttpServletRequest request, RequestControllerData data) throws InvocationTargetException, IllegalAccessException {
        Object controller = getController(data);
        List<Method> controllerMethods = controllerMethodResolver.getControllerMethods(controller, data.getControllerMethod(), request.getParameterMap());
        if (controllerMethods.isEmpty())
            LuisLogger.log(DispatchServlet.class, String.format("Method %s for http: %s not found.", data.getHttpMethod(), data.getControllerMethod()));

        for (Method controllerMethod : controllerMethods) {
            try {
                Object[] args = methodParameterResolver.resolveMethodParameters(request, data, controllerMethod);
                LuisLogger.log(DispatchServlet.class, "Invoking method " + controllerMethod.getName() + " to handle request");
                if (controllerMethod.getParameterCount() > 0)
                    LuisLogger.log(DispatchServlet.class, "Method " + controllerMethod.getName() + " has parameters");

                return controllerMethod.invoke(controller, args);
            } catch (IllegalArgumentException e) {
                logIllegalArgumentException(e, request, controllerMethod);
            }
        }
        return null;
    }

    private Object getController(RequestControllerData data) {
        LuisLogger.log(DispatchServlet.class, "Searching for controller instance");
        Object controller = ComponentsInstances.instances.get(data.getControllerClass());
        if (controller == null) {
            throw new RuntimeException("Controller not found");
        }
        return controller;
    }

    private void logIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request, Method controllerMethod) {
        String methodParametersTypes = Arrays.stream(controllerMethod.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", "));
        String parameters = "(" + methodParametersTypes + ")";
        String method = controllerMethod.getName() + parameters;
        String message = String.format("Method '%s' dont matches the request to path: '%s'", method, request.getRequestURI());
        LuisLogger.log(this.getClass(), message, e);
    }

    private PrintWriter writeResponse(Object result, PrintWriter responseWriter) {
        PrintWriter writer = new PrintWriter(responseWriter);
        writer.println(gson.toJson(result));
        return writer;
    }
}
