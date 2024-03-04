package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import org.example.web.parameter.mock.MockServletInputStream;
import org.example.web.exception.RequestBodyNotFoundException;
import org.example.web.parameter.mock.MockController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.example.web.parameter.DefaultMethodParameterResolverTest.getMethodOfClassWithName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestBodyResolverTest {

    private MethodParameterResolver resolver;
    private List<ArgumentResolverTemplate> resolvers;

    @BeforeEach
    void setup(){
        resolvers = List.of(new RequestBodyResolver());
        resolver = new DefaultMethodParameterResolver(resolvers);
    }

    @Test
    void testResolveMethodParameters_LuisBody() throws IOException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(MockController.class, "methodWithBody");
        String methodUri = "/some/uri";

        when(mockRequest.getInputStream()).thenReturn(new MockServletInputStream("{\"key\": \"value\"}"));

        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(1, resolvedParameters.length);
        assertEquals("value", ((MockController.SomeData) resolvedParameters[0]).getKey());
    }

    @Test
    void testResolveMethodParameters_MissingBody() throws IOException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(MockController.class, "withMultipleParams");
        String methodUri = "/some/uri/{key}";

        when(mockRequest.getInputStream())
                .thenReturn(new MockServletInputStream(""));

        // Missing body in the request
        assertThrows(RequestBodyNotFoundException.class, () ->
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
