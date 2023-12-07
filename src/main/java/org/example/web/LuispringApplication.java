package org.example.web;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.example.explore.ClassExplorer;
import org.example.util.LuisLogger;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LuispringApplication {

    public void run(Class<?> sourceClass) {

        List<String> allClasses = ClassExplorer.retrieveAllClasses(sourceClass);

        Logger.getLogger("org.apache").setLevel(Level.OFF);
        LuisLogger.showBanner();
        LuisLogger.log(LuispringApplication.class, "Starting Application");
        long startupInit = System.currentTimeMillis();

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

        long startupEnd = System.currentTimeMillis();
        LuisLogger.log(LuispringApplication.class, "LuisSpring Web Application started in: " + (startupEnd-startupInit) + "ms");
        tomcat.getServer().await();
    }
}
