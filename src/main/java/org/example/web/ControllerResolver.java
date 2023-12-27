package org.example.web;

import jakarta.servlet.http.HttpServletRequest;
import org.example.datastructures.RequestControllerData;

import java.util.Optional;

public interface ControllerResolver {
    Optional<RequestControllerData> findController(HttpServletRequest request);
}
