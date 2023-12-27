package org.example.web;

public interface ControllerInstanceResolver {
    Object getController(String controllerClassName);
}
