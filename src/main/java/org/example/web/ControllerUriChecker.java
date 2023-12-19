package org.example.web;

public class ControllerUriChecker {
    public boolean matches(String methodUri, String requestUri) {
        String[] methodUriTokens = methodUri.split("/");
        String[] requestUriTokens = requestUri.split("/");

        if (methodUri.equals(requestUri))
            return true;

        if (methodUriTokens.length != requestUriTokens.length)
            return false;

        for (int t = 0; t < methodUriTokens.length; t++) {
            String methodUriToken = methodUriTokens[t];
            String requestUriToken = requestUriTokens[t];

            if (methodUriToken.contains("{") && methodUriToken.contains("}") &&
                !methodUriToken.equals(requestUriToken))
                return true;
            else if (!methodUriToken.equals(requestUriToken))
                return false;
        }

        return false;
    }
}
