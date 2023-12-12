package org.example.web;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.example.boot.ComponentLoader;
import org.example.explore.ClassExplorer;
import org.example.metadata.MetadataExtractor;
import org.example.metadata.MetadataExtractorImpl;
import org.example.util.LuisLogger;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LuisSpringApplication {

    public static void run(Class<?> sourceClass) {
        disableApacheLogs();
        LuisLogger.showBanner();
        LuisLogger.log(LuisSpringApplication.class, "Starting Application");
        long startupInit = System.currentTimeMillis();

        List<String> allClasses = ClassExplorer.retrieveAllClasses(sourceClass);
        MetadataExtractor metadataExtractor = new MetadataExtractorImpl(allClasses);
        metadataExtractor.extractMetadata();
        ComponentLoader loader = new ComponentLoader();
        loader.loadComponents(allClasses);
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
}
