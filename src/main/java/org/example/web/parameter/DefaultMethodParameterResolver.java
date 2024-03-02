package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class DefaultMethodParameterResolver implements MethodParameterResolver {

    private final List<ArgumentResolverTemplate> resolvers;

    public DefaultMethodParameterResolver(List<ArgumentResolverTemplate> resolvers) {
        this.resolvers = resolvers;
        if (resolvers == null || resolvers.isEmpty()) {
            throw new IllegalArgumentException("Resolvers cannot be empty.");
        }
    }

    @Override
    public Object[] resolveMethodParameters(HttpServletRequest request, Method controllerMethod, String methodMappedUri) {
        return resolvers.stream()
                .map(r -> r.resolveMethodParameters(request, controllerMethod, methodMappedUri))
                .flatMap(Collection::stream)
                .toArray();
    }

}
