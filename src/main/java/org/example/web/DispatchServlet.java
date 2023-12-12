package org.example.web;


import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.datastructures.*;
import org.example.util.LuisLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class DispatchServlet extends HttpServlet {

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURL().toString().endsWith("/favicon.ico"))
            return;
        PrintWriter writer = new PrintWriter(response.getWriter());
        Gson gson = new Gson();

        String uri = request.getRequestURI();
        String httpMethod = request.getMethod().toUpperCase();
        String key = httpMethod + uri;
        RequestControllerData data = ControllersMap.values.get(key);
        LuisLogger.log(DispatchServlet.class, "URI:" + uri + "(" + httpMethod + ") - Handler " + data.getControllerMethod());

        Object result = null;
        LuisLogger.log(DispatchServlet.class, "Searching for controller instance");
        try {
            Object controller = ControllersInstances.instances.get(data.getControllerClass());
            if (controller == null) {
                LuisLogger.log(DispatchServlet.class, "Creating new controller instance");
                controller = Class.forName(data.getControllerClass()).getDeclaredConstructor().newInstance();
                ControllersInstances.instances.put(data.getControllerClass(), controller);
                injectDependencies(controller);
            }

            Method controllerMethod = null;
            for (Method each : controller.getClass().getMethods()) {
                if (each.getName().equals(data.getControllerMethod())) {
                    controllerMethod = each;
                    break;
                }
            }
            LuisLogger.log(DispatchServlet.class, "Invoking method " + controllerMethod.getName() + " to handle request");
            if (controllerMethod.getParameterCount() > 0) {
                LuisLogger.log(DispatchServlet.class, "Method " + controllerMethod.getName() + " has parameters");
                Object arg;
                Parameter parameter = controllerMethod.getParameters()[0];
                if (parameter.getAnnotations()[0].annotationType().getSimpleName().equals("LuisBody")) {
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


        writer.println(gson.toJson(result));
        writer.close();
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

    private void injectDependencies(Object client) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for(Field attribute : client.getClass().getDeclaredFields()){
            String attributeTypeName = attribute.getType().getName();
            Object serviceImpl = null;
            if(DependencyInjectionMap.objects.get(attributeTypeName) == null){
                LuisLogger.log(DispatchServlet.class, "Couldn't find instance for " + attributeTypeName);
                String implTypeName = ServiceImplementationMap.implementations.get(attributeTypeName);
                if(implTypeName != null){
                    LuisLogger.log(DispatchServlet.class, "Found instance for " + implTypeName);
                    serviceImpl = DependencyInjectionMap.objects.get(implTypeName);
                    if(serviceImpl == null){
                        LuisLogger.log(DispatchServlet.class, "Found instance for " + implTypeName);
                        serviceImpl = Class.forName(implTypeName).getDeclaredConstructor().newInstance();
                        DependencyInjectionMap.objects.put(implTypeName, serviceImpl);
                    }
                }
            }
            if(serviceImpl != null) {
                attribute.setAccessible(true);
                attribute.set(client, serviceImpl);
                LuisLogger.log(DispatchServlet.class, "Object injected successfully");
            }
        }
    }
}
