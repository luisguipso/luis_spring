package org.example.web;

import java.util.Optional;

public class ControllerUriChecker {
    public boolean matches(String methodUri, String requestUri) {
        String[] methodUriTokens = methodUri.split("/");
        String[] requestUriTokens = requestUri.split("/");

        if (methodUri.equals(requestUri))
            return true;

        if (methodUriTokens.length != requestUriTokens.length)
            return false;

        Optional<Boolean> tokensMatch = tokensMatch(methodUriTokens, requestUriTokens);
        return tokensMatch.orElse(false);

    }

    private Optional<Boolean> tokensMatch(String[] methodUriTokens, String[] requestUriTokens){
        for (int t = 0; t < methodUriTokens.length; t++) {
            String methodUriToken = methodUriTokens[t];
            String requestUriToken = requestUriTokens[t];
            if (isAPathParameter(methodUriToken, requestUriToken))
                return Optional.of(true);
            else if (!methodUriToken.equals(requestUriToken))
                return Optional.of(false);
        }
        return Optional.empty();
    }

    private static boolean isAPathParameter(String methodUriToken, String requestUriToken) {
        return methodUriToken.contains("{") && methodUriToken.contains("}") &&
               !methodUriToken.equals(requestUriToken);
    }
}
