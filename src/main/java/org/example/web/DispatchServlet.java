package org.example.web;


import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.datastructures.ControllersInstances;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

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
        LuisLogger.log(DispatchServlet.class, "URI:"+uri+"("+httpMethod+") - Handler "+data.getControllerMethod());

        Object result = null;
        LuisLogger.log(DispatchServlet.class, "Searching for controller instance");
        try {
            Object controller = ControllersInstances.instances.get(data.getControllerClass());
            if (controller == null) {
                LuisLogger.log(DispatchServlet.class, "Creating new controller instance");
                controller = Class.forName(data.getControllerClass()).getDeclaredConstructor().newInstance();
                ControllersInstances.instances.put(data.getControllerClass(), controller);
            }

            Method controllerMethod = null;
            for(Method each : controller.getClass().getMethods()){
                if(each.getName().equals(data.getControllerMethod())) {
                    controllerMethod = each;
                    break;
                }
            }

            if(controllerMethod != null) {
                LuisLogger.log(DispatchServlet.class, "Invoking method "+controllerMethod.getName()+" to handle request");
                result = controllerMethod.invoke(controller);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }




        writer.println(gson.toJson(result));
        writer.close();
    }
}
