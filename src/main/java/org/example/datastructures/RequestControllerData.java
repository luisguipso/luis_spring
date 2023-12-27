package org.example.datastructures;

import java.lang.reflect.Method;

public class RequestControllerData {
    private String httpMethod;
    private String url;
    private String controllerClass;
    private String methodName;
    private final Method method;


    public RequestControllerData(String httpMethod, String url, String controllerClass, Method method) {
        this(httpMethod, url, controllerClass, method.getName(), method);
    }

    public RequestControllerData(String httpMethod, String url, String controllerClass, String methodName, Method method) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.controllerClass = controllerClass;
        this.methodName = methodName;
        this.method = method;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public String getControllerClass() {
        return controllerClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Method getMethod() {
        return method;
    }
}
