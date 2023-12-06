package org.example;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class LuispringApplication implements Runnable{

    @Override
    public void run() {
        Tomcat tomcat = new Tomcat();
        Connector connector = new Connector();
        connector.setPort(8080);
        tomcat.setConnector(connector);

        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext("", docBase);

        String servletName = "DispatchServlet";
        String contextPath = "/*";
        Tomcat.addServlet(context, servletName, new DispatchServlet());
        context.addServletMappingDecoded(contextPath, servletName);

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }

        tomcat.getServer().await();
    }
}
