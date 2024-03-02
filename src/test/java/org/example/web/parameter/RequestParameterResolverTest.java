package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import org.example.web.exception.RequestParamNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.example.web.parameter.DefaultMethodParameterResolverTest.getMethodOfClassWithName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestParameterResolverTest {

    private List<ArgumentResolverTemplate> resolvers;
    private MethodParameterResolver resolver;

    @BeforeEach
    void setup(){
        resolvers = List.of(new RequestParameterResolver());
        resolver = new DefaultMethodParameterResolver(resolvers);
    }
    @Test
    void testResolveMethodParameters_RequestParam() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class,"methodWithRequestParam");
        String methodUri = "/some/uri/";

        when(mockRequest.getParameter("paramName"))
                .thenReturn("paramValue");

        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);


        assertEquals(1, resolvedParameters.length);
        assertEquals("paramValue", resolvedParameters[0]);
    }

    @Test
    void testResolveMethodParameters_TwoRequestParams() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class,"methodWithTwoRequestParam");
        String methodUri = "/some/uri/";

        when(mockRequest.getParameter("paramName"))
                .thenReturn("paramValue");
        when(mockRequest.getParameter("anotherParam"))
                .thenReturn("anotherValue");

        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);


        assertEquals(2, resolvedParameters.length);
        assertEquals("paramValue", resolvedParameters[0]);
        assertEquals("anotherValue", resolvedParameters[1]);
    }

    @Test
    void testResolveMethodParameters_MissingRequestParam() throws IOException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class, "methodWithRequestParam");
        String methodUri = "/some/uri";
        when(mockRequest.getRequestURI())
                .thenReturn("/some/uri");

        // Missing path variable in the request
        assertThrows(RequestParamNotFoundException.class, () ->
                resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri)
        );
    }

    @Test
    void testResolveMethodWithNoParameters() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class, "methodWithNoParams");
        String methodUri = "/some/uri";

        Object[] resolvedParametersWithoutBody = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(0, resolvedParametersWithoutBody.length);
    }
}
