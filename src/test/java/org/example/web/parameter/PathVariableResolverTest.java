package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import org.example.web.parameter.mock.MockServletInputStream;
import org.example.web.exception.PathVariableNotFoundException;
import org.example.web.parameter.mock.MockController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.example.web.parameter.DefaultMethodParameterResolverTest.getMethodOfClassWithName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PathVariableResolverTest {

    private List<ArgumentResolverTemplate> resolvers;
    private MethodParameterResolver resolver;

    @BeforeEach
    void setup() {
        resolvers = List.of(new PathVariableResolver());
        resolver = new DefaultMethodParameterResolver(resolvers);
    }

    @Test
    void testResolveMethodParameters_PathVariable() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(MockController.class, "methodWithPathVariable");
        String methodUri = "/some/uri/{key}";

        when(mockRequest.getRequestURI())
                .thenReturn("/some/uri/valueOnPath");

        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(1, resolvedParameters.length);
        assertEquals("valueOnPath", resolvedParameters[0]);
    }

    @Test
    void testResolveMethodParameters_WithTwoPathVariables() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(MockController.class, "methodWithTwoPathVariables");
        String methodUri = "/some/uri/{key}/lock/{lock}";

        when(mockRequest.getRequestURI())
                .thenReturn("/some/uri/Doorskey/lock/Windowslock");

        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(2, resolvedParameters.length);
        assertEquals("Doorskey", resolvedParameters[0]);
        assertEquals("Windowslock", resolvedParameters[1]);
    }

    @Test
    void testResolveMethodParameters_MissingPathVariable() throws IOException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(MockController.class, "methodWithPathVariable");
        String methodUri = "/some/uri/{key}";
        when(mockRequest.getInputStream())
                .thenReturn(new MockServletInputStream("{\"key\": \"valueOnBody\"}"));

        when(mockRequest.getRequestURI())
                .thenReturn("/some/uri");

        // Missing path variable in the request
        assertThrows(PathVariableNotFoundException.class, () ->
                resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri)
        );
    }

    @Test
    void testResolveMethodWithNoParameters() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(MockController.class, "methodWithNoParams");
        String methodUri = "/some/uri";

        Object[] resolvedParametersWithoutBody = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(0, resolvedParametersWithoutBody.length);
    }
}
