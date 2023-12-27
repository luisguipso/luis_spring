package org.example.web;

import jakarta.servlet.http.HttpServletRequest;
import org.example.datastructures.RequestControllerData;

import java.lang.reflect.Method;


public interface MethodParameterResolver {
    Object[] resolveMethodParameters(HttpServletRequest request, RequestControllerData data, Method controllerMethod);
}
