package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.example.annotation.LuisPathVariable;
import org.example.web.exception.PathVariableNotFoundException;

public class PathVariableResolver implements ArgumentResolverTemplate {
    @Override public boolean isApplicable(Parameter parameter) {
        return hasLuisPathVariableAnnotation(parameter);
    }

    @Override public String getArgument(HttpServletRequest request, Parameter parameter, String methodMapperUri) {
        return getArgumentFromPathVariable(request, parameter, methodMapperUri);
    }

    private boolean hasLuisPathVariableAnnotation(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations()).anyMatch(LuisPathVariable.class::isInstance);
    }

    private String getArgumentFromPathVariable(HttpServletRequest request, Parameter methodParameter, String methodMappedUri) {
        var paramName = getParameterMappedName(methodParameter);
        var requestURI = request.getRequestURI();
        var methodUriTokens = methodMappedUri.split("/");
        var requestTokens = requestURI.split("/");
        int index = getIndexOfMappedParameter(methodUriTokens, paramName);
        var errorMessage = String.format(PATH_VARIABLE_NOT_FOUND_MSG, paramName, requestURI);
        return getRequestTokenOrThrow(
                requestTokens,
                index,
                cause -> new PathVariableNotFoundException(errorMessage, cause));
    }

    private static String getParameterMappedName(Parameter methodParameter) {
        return Arrays.stream(methodParameter.getAnnotations())
                .filter(LuisPathVariable.class::isInstance)
                .map(each -> ((LuisPathVariable) each).value())
                .findFirst()
                .orElseThrow();
    }

    private static int getIndexOfMappedParameter(String[] methodUriTokens, String paramName) {
        return IntStream.range(0, methodUriTokens.length)
                .filter(i -> PATH_VARIABLE_REGEX_PATTERN.asPredicate().test(methodUriTokens[i]))
                .filter(i -> withoutPathVariableMarkers(methodUriTokens[i]).equals(paramName))
                .findFirst()
                .orElseThrow();
    }

    private static String withoutPathVariableMarkers(String methodUriTokens) {
        return PATH_VARIABLE_REGEX_PATTERN.matcher(methodUriTokens).replaceAll("");
    }

    private static String getRequestTokenOrThrow(
            String[] requestTokens, int index,
            Function<RuntimeException, PathVariableNotFoundException> throable
    ) {
        try {
            return requestTokens[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw throable.apply(ex);
        }
    }
}
