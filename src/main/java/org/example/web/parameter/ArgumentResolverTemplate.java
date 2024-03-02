package org.example.web.parameter;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.example.util.LuisLogger;

public interface ArgumentResolverTemplate {

    String PARAMETER_FOUND_FROM_REQ_TYPE_MSG = "    Parameter of type: %s, found on request.";
    String PARAMETER_CONTENT_MSG = "    Parameter content: %s";
    String REQUEST_PARAMETER_NOT_FOUND_MSG = "Parameter: %s not found in request to: %s";
    String PATH_VARIABLE_NOT_FOUND_MSG = "Variable: '%s' not found in request to: '%s'";
    String ERROR_WHEN_READING_BODY_FROM_REQUEST_MSG = "Error when reading body from request to: %s";
    String REQUEST_BODY_FOR_REQUEST_MUST_NOT_BE_EMPTY_MSG = "Request body for request uri: '%s' must not be empty.";
    Pattern PATH_VARIABLE_REGEX_PATTERN = Pattern.compile("[{}]");
    record ParamArg(Parameter param, String arg){}

    default List<Object> resolveMethodParameters(HttpServletRequest request, Method controllerMethod, String methodMappedUri) {
        return Arrays.stream(controllerMethod.getParameters())
                .filter(this::isApplicable)
                .map(parameter -> new ParamArg(parameter, getArgument(request, parameter, methodMappedUri)))
                .peek(this::logParameterFound)
                .map(ArgumentResolverTemplate::getUnmarshalledArgument)
                .toList();
    }

    private static Object getUnmarshalledArgument(ParamArg paramArg) {
        return new Gson().fromJson(paramArg.arg(), paramArg.param().getType());
    }

    boolean isApplicable(Parameter parameter);

    String getArgument(HttpServletRequest request, Parameter parameter, String methodMapperUri);

    private void logParameterFound(ParamArg paramArg) {
        LuisLogger.log(getClass(), String.format(PARAMETER_FOUND_FROM_REQ_TYPE_MSG, paramArg.param().getType().getName()));
        LuisLogger.log(getClass(), String.format(PARAMETER_CONTENT_MSG, paramArg.arg()));
    }
}
