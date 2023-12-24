package org.example.datastructures;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class RequestControllerData {
    private String httpMethod;
    private String url;
    private String controllerClass;
    private String controllerMethod;

    private List<Parameter> pathVariables;

    public RequestControllerData() {
    }

    public RequestControllerData(String httpMethod, String url, String controllerClass, String controllerMethod) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
        this.pathVariables = new ArrayList<>();
    }

    public RequestControllerData(String httpMethod, String url, String controllerClass, String controllerMethod, List<Parameter> pathVariables) {
        this(httpMethod, url, controllerClass, controllerMethod);
        this.pathVariables = pathVariables;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    public String getControllerMethod() {
        return controllerMethod;
    }

    public void setControllerMethod(String controllerMethod) {
        this.controllerMethod = controllerMethod;
    }

    public List<Parameter> getPathVariables() {
        return pathVariables;
    }
}
