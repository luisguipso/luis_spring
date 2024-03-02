package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.example.annotation.LuisBody;
import org.example.util.LuisLogger;
import org.example.web.exception.RequestBodyNotFoundException;

public class RequestBodyResolver implements ArgumentResolverTemplate {

    @Override public boolean isApplicable(Parameter parameter) {
        return hasLuisBodyAnnotation(parameter);
    }

    @Override public String getArgument(HttpServletRequest request, Parameter parameter, String methodMapperUri) {
        return getArgumentFromRequestBody(request);
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
}
