package org.example.boot;

import org.example.util.ClassUtil;
import org.example.util.LuisLogger;
import org.example.web.DispatchServlet;

public class ComponentFactory {

    public static Object getComponent(String className) {
        LuisLogger.log(DispatchServlet.class, "Creating new component instance, for: " + className);
        try {
            return ClassUtil.getClass(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
