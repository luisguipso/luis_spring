package org.example.web;


import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.annotation.LuisBody;
import org.example.annotation.LuisPathVariable;
import org.example.annotation.LuisRequestParam;
import org.example.datastructures.ComponentsInstances;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DispatchServlet extends HttpServlet {

    public static final String PARAMETER_FOUND_FROM_REQ_TYPE_MSG = "    Parameter of type: %s, found on request.";
    public static final String PARAMETER_CONTENT_MSG = "    Parameter content: %s";
    private Gson gson;

    public DispatchServlet() {
        this.gson = new Gson();
    }
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURL().toString().endsWith("/favicon.ico"))
            return;

        Optional<RequestControllerData> foundData = ControllerMatcher.searchController(request);
        if (foundData.isEmpty()) {
            response.sendError(404, "Not Found.");
            return;
        }
        RequestControllerData data = foundData.get();

        try {
            Object result = getResultFromControllerMethod(request, data);
            writeResponse(result, response.getWriter()).close();
        } catch (IOException | InvocationTargetException | IllegalAccessException e){
            e.printStackTrace();
            response.sendError(500, e.getMessage());
        }
    }

    Object getResultFromControllerMethod(HttpServletRequest request, RequestControllerData data) throws IOException, InvocationTargetException, IllegalAccessException {
        Object controller = getController(data);
        List<Method> controllerMethods = getMethod(controller, data, request.getParameterMap());
        if(controllerMethods.isEmpty())
            LuisLogger.log(DispatchServlet.class, String.format("Method %s for http: %s not found.", data.getHttpMethod(), data.getControllerMethod()));


        for(Method controllerMethod : controllerMethods) {
            try {
                LuisLogger.log(DispatchServlet.class, "Invoking method " + controllerMethod.getName() + " to handle request");


            if (controllerMethod.getParameterCount() > 0)
                return invokeMethodWithArguments(request, data, controller, controllerMethod);

            return controllerMethod.invoke(controller);
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

    private static List<Method> getMethod(Object controller, RequestControllerData data, Map<String, String[]> parameterMap) {
        Method[] methods = controller.getClass().getMethods();

        Predicate<Method> predicate = getMethodPredicate(data, parameterMap);
        return Arrays.stream(methods)
                .filter(predicate)
                .toList();
    }

    private static Predicate<Method> getMethodPredicate(RequestControllerData data, Map<String, String[]> parameterMap) {
        Predicate<Method> predicate = each -> each.getName().equals(data.getControllerMethod());

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

    private Object invokeMethodWithArguments(HttpServletRequest request, RequestControllerData data, Object controller, Method controllerMethod) throws IOException, IllegalAccessException, InvocationTargetException {
        LuisLogger.log(DispatchServlet.class, "Method " + controllerMethod.getName() + " has parameters");
        List<Object> args = new ArrayList<>();
        for (Parameter parameter : controllerMethod.getParameters()) {
            Optional<String> argument = Optional.empty();
            if (hasLuisBodyAnnotation(parameter)) {
                argument = getArgumentFromRequestBody(request, parameter);
            } else if (hasLuisPathVariableAnnotation(parameter)) {
                argument = getArgumentFromPathVariable(request, parameter, data);
            } else if (hasLuisRequestParamAnnotation(parameter)) {
                argument = getArgumentFromRequestParam(request, parameter);
            }
            if (argument.isEmpty())
                continue;

            args.add(gson.fromJson(argument.get(), parameter.getType()));
            logParameterFound(parameter, argument.get());
        }
        return controllerMethod.invoke(controller, args.toArray());
    }

    private static boolean hasLuisBodyAnnotation(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations()).anyMatch(LuisBody.class::isInstance);
    }

    private Optional<String> getArgumentFromRequestBody(HttpServletRequest request, Parameter parameter) throws IOException {
        return Optional.ofNullable(readBytesFromRequest(request));
    }

    private String readBytesFromRequest(HttpServletRequest request) throws IOException {
        StringBuilder str = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        while ((line = br.readLine()) != null) {
            str.append(line);
        }
        return str.toString();
    }

    private static boolean hasLuisPathVariableAnnotation(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations()).anyMatch(LuisPathVariable.class::isInstance);
    }

    private Optional<String> getArgumentFromPathVariable(HttpServletRequest request, Parameter parameter, RequestControllerData data) {
        String paramName = Arrays.stream(parameter.getAnnotations())
                .filter(LuisPathVariable.class::isInstance)
                .map(each -> ((LuisPathVariable) each).value())
                .findFirst().orElseThrow();
        String argument = readVariableFromPath(paramName, data.getUrl(), request.getRequestURI());
        return Optional.ofNullable(argument);
    }

    private String readVariableFromPath(String paramName, String methodUrl, String requestURI) {
        String[] methodUrlTokens = methodUrl.split("/");
        for (int i = 0; i < methodUrlTokens.length; i++)
            if (methodUrlTokens[i].replace("{", "").replace("}", "").equals(paramName))
                return requestURI.split("/")[i];

        throw new RuntimeException("Could not read variable '" + paramName + "' from path: " + requestURI);
    }

    private static boolean hasLuisRequestParamAnnotation(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations()).anyMatch(LuisRequestParam.class::isInstance);
    }

    private static Optional<String> getArgumentFromRequestParam(HttpServletRequest request, Parameter parameter) {
        String paramName = Arrays.stream(parameter.getAnnotations())
                .filter(LuisRequestParam.class::isInstance)
                .map(each -> ((LuisRequestParam) each).value())
                .findFirst().orElseThrow();
        String param = request.getParameter(paramName);
        return Optional.ofNullable(param);
    }

    private static void logParameterFound(Parameter parameter, String argument) {
        LuisLogger.log(DispatchServlet.class, String.format(PARAMETER_FOUND_FROM_REQ_TYPE_MSG, parameter.getType().getName()));
        LuisLogger.log(DispatchServlet.class, String.format(PARAMETER_CONTENT_MSG, argument));
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
