package org.example.boot;

import org.example.datastructures.ComponentsInstances;

import java.util.List;

public class ComponentLoader {

    public void loadComponents(List<String> allClasses) {
        allClasses.stream()
                .filter(ComponentUtils::isALuisComponent)
                .forEach(className -> {
                    Object component = ComponentFactory.createComponent(className);
                    ComponentsInstances.instances.put(className, component);
                });
    }
}
