package org.example.metadata;

import org.example.annotation.LuisGetMethod;
import org.example.annotation.LuisPostMethod;
import org.example.annotation.LuisRequestParam;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;
import org.example.web.LuisSpringApplication;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.Predicate;

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

                RequestControllerData data = new RequestControllerData(httpMethod, path, className, method);
                ControllersMap.values.put(httpMethod + path + "/p=" + getParameterCount(method), data);
            }
        }
        logFoundHttpMethods();
    }

    private static long getParameterCount(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(isLuisRequestParamAnnotated())
                .count();
    }

    private static Predicate<Parameter> isLuisRequestParamAnnotated() {
        return p -> Arrays.stream(p.getAnnotations())
                .anyMatch(LuisRequestParam.class::isInstance);
    }

    private static void logFoundHttpMethods() {
        for(RequestControllerData each : ControllersMap.values.values())
            LuisLogger.log(LuisSpringApplication.class, "    " + each.getHttpMethod() + ":" + each.getUrl() + " [" + each.getControllerClass() + "." + each.getMethod().getName() + "]");
    }
}
