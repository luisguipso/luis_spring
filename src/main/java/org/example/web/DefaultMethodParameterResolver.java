package org.example.web;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.example.annotation.LuisBody;
import org.example.annotation.LuisPathVariable;
import org.example.annotation.LuisRequestParam;
import org.example.util.LuisLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Method;
import java.util.Optional;
import org.example.web.exception.RequestBodyNotFoundException;
import org.example.web.exception.PathVariableNotFoundException;
import org.example.web.exception.RequestParamNotFoundException;
import static java.util.Optional.of;

public class DefaultMethodParameterResolver implements MethodParameterResolver {

    public static final String PARAMETER_FOUND_FROM_REQ_TYPE_MSG = "    Parameter of type: %s, found on request.";
    public static final String PARAMETER_CONTENT_MSG = "    Parameter content: %s";

    private final Gson gson;
    public DefaultMethodParameterResolver(){
        gson = new Gson();
    }

    @Override
    public Object[] resolveMethodParameters(HttpServletRequest request, Method controllerMethod, String methodUri) {
        List<Object> args = new ArrayList<>();
        for (Parameter parameter : controllerMethod.getParameters()) {
            Optional<String> argument = Optional.empty();
            if (hasLuisBodyAnnotation(parameter)) {
                argument = of(getArgumentFromRequestBody(request));
            } else if (hasLuisPathVariableAnnotation(parameter)) {
                argument = of(getArgumentFromPathVariable(request, parameter, methodUri));
            } else if (hasLuisRequestParamAnnotation(parameter)) {
                argument = of(getArgumentFromRequestParam(request, parameter));
            }
            if (argument.isEmpty())
                continue;

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
        if(body == null || body.isEmpty())
            throw new RequestBodyNotFoundException(String.format("Request body for request uri: '%s' must not be empty.", request.getRequestURI()));
        return body;
    }

    private String readBytesFromRequest(HttpServletRequest request) {
        StringBuilder str = new StringBuilder();
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            while ((line = br.readLine()) != null) {
                str.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    private boolean hasLuisPathVariableAnnotation(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations()).anyMatch(LuisPathVariable.class::isInstance);
    }

    private String getArgumentFromPathVariable(HttpServletRequest request, Parameter parameter, String methodUri) {
        String paramName = Arrays.stream(parameter.getAnnotations())
                .filter(LuisPathVariable.class::isInstance)
                .map(each -> ((LuisPathVariable) each).value())
                .findFirst()
                .orElseThrow();
        return readVariableFromPath(paramName, methodUri, request.getRequestURI());
    }

    private String readVariableFromPath(String paramName, String methodUrl, String requestURI) {
        String[] methodUrlTokens = methodUrl.split("/");
        for (int i = 0; i < methodUrlTokens.length; i++)
            if (methodUrlTokens[i].replaceAll("[{}]", "").equals(paramName)) {
                String[] requestTokens = requestURI.split("/");
                if(requestTokens.length < i+1)
                    throw new PathVariableNotFoundException(String.format("Variable: '%s' not found in request uri: '%s'", paramName, requestURI));
                return requestTokens[i];
            }

        throw new PathVariableNotFoundException(String.format("Variable: '%s' not found in request uri: '%s'", paramName, requestURI));
    }

    private boolean hasLuisRequestParamAnnotation(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations()).anyMatch(LuisRequestParam.class::isInstance);
    }

    private String getArgumentFromRequestParam(HttpServletRequest request, Parameter parameter) {
        String errorMessage = String.format("Parameter: %s not found in request to: %s", parameter.getName(), request.getRequestURI());
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
