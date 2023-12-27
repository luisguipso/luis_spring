package org.example.web;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
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
                argument = getArgumentFromRequestBody(request);
            } else if (hasLuisPathVariableAnnotation(parameter)) {
                argument = getArgumentFromPathVariable(request, parameter, methodUri);
            } else if (hasLuisRequestParamAnnotation(parameter)) {
                argument = getArgumentFromRequestParam(request, parameter);
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

    private Optional<String> getArgumentFromRequestBody(HttpServletRequest request) {
        return Optional.of(readBytesFromRequest(request));
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

    private Optional<String> getArgumentFromPathVariable(HttpServletRequest request, Parameter parameter, String methodUri) {
        String paramName = Arrays.stream(parameter.getAnnotations())
                .filter(LuisPathVariable.class::isInstance)
                .map(each -> ((LuisPathVariable) each).value())
                .findFirst().orElseThrow();
        String argument = readVariableFromPath(paramName, methodUri, request.getRequestURI());
        return Optional.ofNullable(argument);
    }

    private String readVariableFromPath(String paramName, String methodUrl, String requestURI) {
        String[] methodUrlTokens = methodUrl.split("/");
        for (int i = 0; i < methodUrlTokens.length; i++)
            if (methodUrlTokens[i].replaceAll("[{}]", "").equals(paramName))
                return requestURI.split("/")[i];


        throw new RuntimeException("Could not read variable '" + paramName + "' from path: " + requestURI);
    }

    private boolean hasLuisRequestParamAnnotation(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations()).anyMatch(LuisRequestParam.class::isInstance);
    }

    private Optional<String> getArgumentFromRequestParam(HttpServletRequest request, Parameter parameter) {
        String paramName = Arrays.stream(parameter.getAnnotations())
                .filter(LuisRequestParam.class::isInstance)
                .map(each -> ((LuisRequestParam) each).value())
                .findFirst().orElseThrow();
        String param = request.getParameter(paramName);
        return Optional.ofNullable(param);
    }

    private void logParameterFound(Parameter parameter, String argument) {
        LuisLogger.log(this.getClass(), String.format(PARAMETER_FOUND_FROM_REQ_TYPE_MSG, parameter.getType().getName()));
        LuisLogger.log(this.getClass(), String.format(PARAMETER_CONTENT_MSG, argument));
    }

}
