package org.example.web;

import jakarta.servlet.http.HttpServletRequest;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.util.LuisLogger;

import java.util.Map;
import java.util.Optional;

public class DefaultControllerDataResolver implements ControllerDataResolver {
    @Override
    public Optional<RequestControllerData> findController(HttpServletRequest request) {
        if(request == null)
            return Optional.empty();

        String key = request.getMethod() + request.getRequestURI() + "/p=" + request.getParameterMap().size();
        Optional<RequestControllerData> foundData = searchForControllerDirectly(key);
        if (foundData.isEmpty())
            foundData = searchControllerWithPathVariable(key);
        foundData.ifPresent(data -> logFoundHandler(request, data));
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

    private static void logFoundHandler(HttpServletRequest request, RequestControllerData foundData) {
        String message = String.format(
                "URI: %s (%s) - Handler: %s",
                request.getRequestURI(),
                request.getMethod(),
                foundData.getMethod().getName());
        LuisLogger.log(DefaultControllerDataResolver.class, message);
    }
}
