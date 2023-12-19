package org.example.web;


import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.annotation.LuisBody;
import org.example.datastructures.ComponentsInstances;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class DispatchServlet extends HttpServlet {

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURL().toString().endsWith("/favicon.ico"))
            return;


        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        Optional<RequestControllerData> foundData = searchForControllerDirectly(requestURI, requestMethod);
        if (foundData.isEmpty())
            foundData = searchControllerWithPathVariable(requestURI, requestMethod);

        RequestControllerData data;
        if (foundData.isEmpty()) {
            response.sendError(404, "Not Found.");
            return;
        }
        data = foundData.get();


        LuisLogger.log(DispatchServlet.class, "URI:" + requestURI + "(" + requestMethod + ") - Handler " + data.getControllerMethod());

        Object result = null;
        LuisLogger.log(DispatchServlet.class, "Searching for controller instance");
        Object controller = getController(data);
        Method controllerMethod = getMethod(data, controller);
        LuisLogger.log(DispatchServlet.class, "Invoking method " + controllerMethod.getName() + " to handle request");

        Gson gson = new Gson();
        try {
            if (controllerMethod.getParameterCount() > 0) {
                LuisLogger.log(DispatchServlet.class, "Method " + controllerMethod.getName() + " has parameters");
                Object arg;
                Parameter parameter = controllerMethod.getParameters()[0];
                if (parameter.getAnnotations()[0] instanceof LuisBody) {
                    String body = readBytesFromRequest(request);
                    LuisLogger.log(DispatchServlet.class, "    Found parameter from request of type " + parameter.getType().getName());
                    LuisLogger.log(DispatchServlet.class, "    Parameter content: " + body);
                    arg = gson.fromJson(body, parameter.getType());
                    result = controllerMethod.invoke(controller, arg);
                }
            } else {
                result = controllerMethod.invoke(controller);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        PrintWriter writer = new PrintWriter(response.getWriter());
        writer.println(gson.toJson(result));
        writer.close();
    }

    private static Optional<RequestControllerData> searchForControllerDirectly(String uri, String httpMethod) {
        String key = httpMethod + uri;
        return Optional.ofNullable(ControllersMap.values.get(key));
    }

    private Optional<RequestControllerData> searchControllerWithPathVariable(String uri, String httpMethod) {
        ControllerUriChecker controllerUriChecker = new ControllerUriChecker();
        return ControllersMap.values.entrySet().stream()
                .filter(each -> controllerUriChecker.matches(each.getKey(), httpMethod+uri))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    private Object getController(RequestControllerData data) {
        Object controller = ComponentsInstances.instances.get(data.getControllerClass());
        if (controller == null) {
            throw new RuntimeException("Controller not found");
        }
        return controller;
    }

    private static Method getMethod(RequestControllerData data, Object controller) {
        Method[] methods = controller.getClass().getMethods();
        String errorMessage = String.format("Method %s for http: %s not found.", data.getHttpMethod(), data.getControllerMethod());

        return Arrays.stream(methods)
                .filter(each -> each.getName().equals(data.getControllerMethod()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(errorMessage));
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


}
