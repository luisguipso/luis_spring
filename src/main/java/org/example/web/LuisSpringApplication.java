package org.example.web;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.example.annotation.LuisGetMethod;
import org.example.annotation.LuisPostMethod;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.explore.ClassExplorer;
import org.example.util.LuisLogger;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LuisSpringApplication {

    public static void run(Class<?> sourceClass) {
        disableApacheLogs();
        LuisLogger.showBanner();
        LuisLogger.log(LuisSpringApplication.class, "Starting Application");
        long startupInit = System.currentTimeMillis();

        extractMetadata(sourceClass);
        Tomcat tomcat = startEmbededTomcat();

        long startupEnd = System.currentTimeMillis();
        LuisLogger.log(LuisSpringApplication.class, "LuisSpring Web Application started in: " + (startupEnd - startupInit) + "ms");
        tomcat.getServer().await();
    }

    private static void disableApacheLogs() {
        Logger.getLogger("org.apache").setLevel(Level.OFF);
    }

    private static Tomcat startEmbededTomcat() {
        Tomcat tomcat = new Tomcat();
        Connector connector = new Connector();
        connector.setPort(8080);
        tomcat.setConnector(connector);

        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext("", docBase);

        String servletName = DispatchServlet.class.getSimpleName();
        String contextPath = "/*";
        Tomcat.addServlet(context, servletName, new DispatchServlet());
        context.addServletMappingDecoded(contextPath, servletName);

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
        return tomcat;
    }

    private static void extractMetadata(Class<?> sourceClass){
        List<String> allClasses = ClassExplorer.retrieveAllClasses(sourceClass);
        try {
            for (String className : allClasses){
                for (Annotation classAnnotation : Class.forName(className).getAnnotations()){
                    if(classAnnotation.annotationType().getSimpleName().equals("LuisController")){
                        LuisLogger.log(LuisSpringApplication.class, "Found a Controller: " + className);
                        extractMethod(className);
                    }
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void extractMethod(String className) throws ClassNotFoundException {
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
                //LuisLogger.log(LuisSpringApplication.class, " + method " + method.getName() + " - URL " + httpMethod + " = " + path);
                RequestControllerData data = new RequestControllerData(httpMethod, path, className, method.getName());
                ControllersMap.values.put(httpMethod + path, data);
            }
        }

        for(RequestControllerData each : ControllersMap.values.values()){
            LuisLogger.log(LuisSpringApplication.class, "    "+each.getHttpMethod() +":"+each.getUrl()+" ["+each.getControllerClass()+"."+each.getControllerMethod()+"]");
        }
    }
}
