package org.example.web;

import org.example.datastructures.ComponentsInstances;
import org.example.util.LuisLogger;
import org.example.web.exception.MethodNotFoundException;

public class DefaultControllerInstanceResolver implements ControllerInstanceResolver {
    @Override
    public Object getController(String controllerClassName) {
        LuisLogger.log(this.getClass(), "Searching for controller instance");
        Object controller = ComponentsInstances.instances.get(controllerClassName);
        if (controller == null) {
            throw new MethodNotFoundException("Controller not found");
        }
        return controller;
    }
}
