package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;


public interface MethodParameterResolver {
    Object[] resolveMethodParameters(HttpServletRequest request, Method controllerMethod, String methodMappedUri);
}
