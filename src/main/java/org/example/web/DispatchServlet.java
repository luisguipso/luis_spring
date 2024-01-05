package org.example.web;


import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.util.LuisLogger;

import java.io.IOException;
import java.io.PrintWriter;
import org.example.web.exception.MethodNotFoundException;

public class DispatchServlet extends HttpServlet {

    private final transient Gson gson;
    private final transient RequestHandler requestHandler;

    public DispatchServlet(RequestHandler requestHandler) {
        gson = new Gson();
        this.requestHandler = requestHandler;
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURL().toString().endsWith("/favicon.ico"))
            return;

        try {
            Object result = requestHandler.handleRequest(request);
            writeResponse(result, response.getWriter()).close();
        } catch (MethodNotFoundException e) {
            LuisLogger.log(this.getClass(), e.getMessage(), e);
            response.sendError(404, "Not Found.");
        } catch (Exception e) {
            LuisLogger.log(this.getClass(), e.getMessage(), e);
            response.sendError(500, e.getMessage());
        }
    }

    private PrintWriter writeResponse(Object result, PrintWriter responseWriter) {
        PrintWriter writer = new PrintWriter(responseWriter);
        writer.println(gson.toJson(result));
        return writer;
    }
}
