package org.example.web.parameter;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.example.annotation.LuisBody;
import org.example.annotation.LuisPathVariable;
import org.example.annotation.LuisRequestParam;
import org.example.util.LuisLogger;
import org.example.web.exception.PathVariableNotFoundException;
import org.example.web.exception.RequestBodyNotFoundException;
import org.example.web.exception.RequestParamNotFoundException;
import static java.util.Optional.of;

public class DefaultMethodParameterResolver implements MethodParameterResolver {

    public static final String PARAMETER_FOUND_FROM_REQ_TYPE_MSG = "    Parameter of type: %s, found on request.";
    public static final String PARAMETER_CONTENT_MSG = "    Parameter content: %s";
    public static final String REQUEST_PARAMETER_NOT_FOUND_MSG = "Parameter: %s not found in request to: %s";
    public static final String PATH_VARIABLE_NOT_FOUND_MSG = "Variable: '%s' not found in request to: '%s'";
    public static final String ERROR_WHEN_READING_BODY_FROM_REQUEST_MSG = "Error when reading body from request to: %s";
    public static final String REQUEST_BODY_FOR_REQUEST_MUST_NOT_BE_EMPTY_MSG = "Request body for request uri: '%s' must not be empty.";
    private static final Pattern PATH_VARIABLE_REGEX_PATTERN = Pattern.compile("[{}]");

    private final Gson gson;

    public DefaultMethodParameterResolver() {
        gson = new Gson();
    }

    @Override
    public Object[] resolveMethodParameters(HttpServletRequest request, Method controllerMethod, String methodMappedUri) {
        List<Object> args = new ArrayList<>();
        for (Parameter parameter : controllerMethod.getParameters()) {
            Optional<String> argument = Optional.empty();
            if (hasLuisBodyAnnotation(parameter)) {
                argument = of(getArgumentFromRequestBody(request));
            } else if (hasLuisPathVariableAnnotation(parameter)) {
                argument = of(getArgumentFromPathVariable(request, parameter, methodMappedUri));
            } else if (hasLuisRequestParamAnnotation(parameter)) {
                argument = of(getArgumentFromRequestParam(request, parameter));
            }
            if (argument.isEmpty()) {continue;}

            args.add(gson.fromJson(argument.get(), parameter.getType()));
            logParameterFound(parameter, argument.get());
        }
        return args.toArray();
    }

    private boolean hasLuisBodyAnnotation(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations()).anyMatch(LuisBody.class::isInstance);
    }

    private String getArgumentFromRequestBody(HttpServletRequest request) {
        String body = readBytesFromRequest(request);
        if (body == null || body.isEmpty()) {
            var errorMessage = String.format(REQUEST_BODY_FOR_REQUEST_MUST_NOT_BE_EMPTY_MSG, request.getRequestURI());
            throw new RequestBodyNotFoundException(errorMessage);
        }
        return body;
    }

    private String readBytesFromRequest(HttpServletRequest request) {
        StringBuilder str = new StringBuilder();
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            while ((line = br.readLine()) != null) {
                str.append(line);
            }
        } catch (IOException e) {
            var errorMessage = String.format(ERROR_WHEN_READING_BODY_FROM_REQUEST_MSG, request.getRequestURI());
            LuisLogger.log(getClass(), errorMessage, e);
        }
        return str.toString();
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

    private static int getIndexOfMappedParameter(String[] methodUriTokens, String paramName) {
        return IntStream.range(0, methodUriTokens.length)
                .filter(i -> PATH_VARIABLE_REGEX_PATTERN.asPredicate().test(methodUriTokens[i]))
                .filter(i -> withoutPathVariableMarkers(methodUriTokens[i]).equals(paramName))
                .findFirst()
                .orElseThrow();
    }

    private static String getParameterMappedName(Parameter methodParameter) {
        return Arrays.stream(methodParameter.getAnnotations())
                .filter(LuisPathVariable.class::isInstance)
                .map(each -> ((LuisPathVariable) each).value())
                .findFirst()
                .orElseThrow();
    }

    private static String withoutPathVariableMarkers(String methodUriTokens) {
        return PATH_VARIABLE_REGEX_PATTERN.matcher(methodUriTokens).replaceAll("");
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

    private void logParameterFound(Parameter parameter, String argument) {
        LuisLogger.log(getClass(), String.format(PARAMETER_FOUND_FROM_REQ_TYPE_MSG, parameter.getType().getName()));
        LuisLogger.log(getClass(), String.format(PARAMETER_CONTENT_MSG, argument));
    }

}
