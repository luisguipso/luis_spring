package org.example.web;


import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.annotation.LuisBody;
import org.example.annotation.LuisPathVariable;
import org.example.datastructures.ComponentsInstances;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DispatchServlet extends HttpServlet {

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

        Object result = null;
        LuisLogger.log(DispatchServlet.class, "Searching for controller instance");
        Object controller = getController(data);
        Method controllerMethod = getMethod(data, controller);
        LuisLogger.log(DispatchServlet.class, "Invoking method " + controllerMethod.getName() + " to handle request");

        Gson gson = new Gson();
        try {
            if (controllerMethod.getParameterCount() > 0) {
                LuisLogger.log(DispatchServlet.class, "Method " + controllerMethod.getName() + " has parameters");
                List<Object> args = new ArrayList<>();
                for (Parameter parameter : controllerMethod.getParameters()) {
                    if (Arrays.stream(parameter.getAnnotations()).anyMatch(LuisBody.class::isInstance)) {
                        String body = readBytesFromRequest(request);
                        LuisLogger.log(DispatchServlet.class, "    Found parameter from request of type " + parameter.getType().getName());
                        LuisLogger.log(DispatchServlet.class, "    Parameter content: " + body);
                        args.add(gson.fromJson(body, parameter.getType()));
                    } else if (Arrays.stream(parameter.getAnnotations()).anyMatch(LuisPathVariable.class::isInstance)) {
                        String paramName = Arrays.stream(parameter.getAnnotations())
                                .filter(LuisPathVariable.class::isInstance)
                                .map(each -> ((LuisPathVariable) each).value())
                                .findFirst().get();
                        String variable = readVariableFromPath(paramName, data.getUrl(), request.getRequestURI());
                        LuisLogger.log(DispatchServlet.class, "    Found parameter from request of type " + parameter.getType().getName());
                        LuisLogger.log(DispatchServlet.class, "    Parameter content: " + variable);
                        args.add(gson.fromJson(variable, parameter.getType()));
                    }
                    result = controllerMethod.invoke(controller, args.toArray());
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

    private String readVariableFromPath(String paramName, String methodUrl, String requestURI) {
        String[] methodUrlTokens = methodUrl.split("/");
        for (int i = 0; i < methodUrlTokens.length; i++)
            if(methodUrlTokens[i].replace("{","").replace("}","").equals(paramName))
                return requestURI.split("/")[i];

        throw new RuntimeException("Could not read variable '"+ paramName + "' from path: " + requestURI);
    }


}
