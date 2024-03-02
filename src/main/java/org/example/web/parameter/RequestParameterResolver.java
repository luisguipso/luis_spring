package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;
import org.example.annotation.LuisRequestParam;
import org.example.web.exception.RequestParamNotFoundException;

public class RequestParameterResolver implements ArgumentResolverTemplate {
    @Override public boolean isApplicable(Parameter parameter) {
        return hasLuisRequestParamAnnotation(parameter);
    }

    @Override public String getArgument(HttpServletRequest request, Parameter parameter, String methodMapperUri) {
        return getArgumentFromRequestParam(request, parameter);
    }

    private boolean hasLuisRequestParamAnnotation(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations())
                .anyMatch(LuisRequestParam.class::isInstance);
    }

    private String getArgumentFromRequestParam(HttpServletRequest request, Parameter parameter) {
        String errorMessage = String.format(REQUEST_PARAMETER_NOT_FOUND_MSG, parameter.getName(), request.getRequestURI());
        return Arrays.stream(parameter.getAnnotations())
                .filter(LuisRequestParam.class::isInstance)
                .map(LuisRequestParam.class::cast)
                .map(LuisRequestParam::value)
                .map(request::getParameter)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RequestParamNotFoundException(errorMessage));
    }
}
