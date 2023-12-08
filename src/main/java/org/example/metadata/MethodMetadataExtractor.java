package org.example.metadata;

import org.example.annotation.LuisGetMethod;
import org.example.annotation.LuisPostMethod;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;
import org.example.web.LuisSpringApplication;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MethodMetadataExtractor{

    public void extractMetadata(String className) throws ClassNotFoundException {
        String httpMethod = "";
        String path = "";
        for(Method method : Class.forName(className).getDeclaredMethods()){
            for(Annotation methodAnnotation : method.getAnnotations()){
                if(methodAnnotation.annotationType().getSimpleName().equals("LuisGetMethod")){
                    path = ((LuisGetMethod)methodAnnotation).value();
                    httpMethod = "GET";
                } else if (methodAnnotation.annotationType().getSimpleName().equals("LuisPostMethod")) {
                    path = ((LuisPostMethod)methodAnnotation).value();
                    httpMethod = "POST";
                }
                RequestControllerData data = new RequestControllerData(httpMethod, path, className, method.getName());
                ControllersMap.values.put(httpMethod + path, data);
            }
        }
        logFoundHttpMethods();
    }

    private static void logFoundHttpMethods() {
        for(RequestControllerData each : ControllersMap.values.values()){
            LuisLogger.log(LuisSpringApplication.class, "    " + each.getHttpMethod() + ":" + each.getUrl() + " [" + each.getControllerClass() + "." + each.getControllerMethod() + "]");
        }
    }
}
