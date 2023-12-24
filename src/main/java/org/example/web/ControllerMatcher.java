package org.example.web;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;

import java.util.Map;
import java.util.Optional;

public class ControllerMatcher {

    public static Optional<RequestControllerData> searchController(@Nonnull HttpServletRequest request) {
        if(request == null)
            return Optional.empty();

        String key = request.getMethod() + request.getRequestURI();
        Optional<RequestControllerData> foundData = searchForControllerDirectly(key);
        if (foundData.isEmpty())
            foundData = searchControllerWithPathVariable(key);
        logFoundHandler(request, foundData);
        return foundData;
    }

    private static Optional<RequestControllerData> searchForControllerDirectly(String key) {
        return Optional.ofNullable(ControllersMap.values.get(key));
    }

    private static Optional<RequestControllerData> searchControllerWithPathVariable(String key) {
        ControllerUriChecker controllerUriChecker = new ControllerUriChecker();
        return ControllersMap.values.entrySet().stream()
                .filter(each -> controllerUriChecker.matches(each.getKey(), key))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    private static void logFoundHandler(HttpServletRequest request, Optional<RequestControllerData> foundData) {
        if (foundData.isEmpty())
            return;

        RequestControllerData data = foundData.get();
        String message = String.format(
                "URI: %s (%s) - Handler: %s",
                request.getRequestURI(),
                request.getMethod(),
                data.getControllerMethod());
        LuisLogger.log(ControllerMatcher.class, message);
    }
}
