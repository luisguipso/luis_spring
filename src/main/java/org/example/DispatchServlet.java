package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class DispatchServlet extends HttpServlet {

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter writer = new PrintWriter(response.getWriter());
        writer.println("<html><h1>Hello Word from " + request.getRequestURI() + "</h1></html>");
        writer.close();
    }
}
