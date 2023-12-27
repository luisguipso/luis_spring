package org.example.web;

import jakarta.servlet.http.HttpServletRequest;

public interface RequestHandler {
    Object handleRequest(HttpServletRequest request) throws Exception;
}
