package org.example.metadata;

import org.example.annotation.LuisGetMethod;
import org.example.annotation.LuisPathVariable;
import org.example.annotation.LuisPostMethod;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;
import org.example.web.LuisSpringApplication;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class MethodMetadataExtractor{

    public void extractMetadata(String className) throws ClassNotFoundException {
        String httpMethod = "";
        String path = "";
        for(Method method : Class.forName(className).getDeclaredMethods()){
            for(Annotation methodAnnotation : method.getAnnotations()){
                if(methodAnnotation instanceof LuisGetMethod luisGetMethod){
                    path = luisGetMethod.value();
                    httpMethod = "GET";
                } else if (methodAnnotation instanceof LuisPostMethod luisPostMethod) {
                    path = luisPostMethod.value();
                    httpMethod = "POST";
                } else {
                    continue;
                }

                List<Parameter> pathParameters = getPathParameters(method);
                RequestControllerData data = new RequestControllerData(httpMethod, path, className, method.getName(), pathParameters);
                ControllersMap.values.put(httpMethod + path, data);
            }
        }
        logFoundHttpMethods();
    }

    private static List<Parameter> getPathParameters(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(each -> Arrays.stream(each.getAnnotations()).anyMatch(LuisPathVariable.class::isInstance))
                .toList();
    }

    private static void logFoundHttpMethods() {
        for(RequestControllerData each : ControllersMap.values.values()){
            LuisLogger.log(LuisSpringApplication.class, "    " + each.getHttpMethod() + ":" + each.getUrl() + " [" + each.getControllerClass() + "." + each.getControllerMethod() + "]");
        }
    }
}
