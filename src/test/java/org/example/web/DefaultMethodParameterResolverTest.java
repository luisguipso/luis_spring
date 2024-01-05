package org.example.web;

import org.junit.jupiter.api.Test;
import org.example.annotation.LuisBody;
import org.example.annotation.LuisPathVariable;
import org.example.annotation.LuisRequestParam;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultMethodParameterResolverTest {
    @Test
    void testResolveMethodParameters_LuisBody() throws IOException {
        DefaultMethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = SomeController.class.getDeclaredMethods()[0]; // Replace with your controller method
        String methodUri = "/some/uri";

        when(mockRequest.getInputStream()).thenReturn(new MockServletInputStream("{\"key\": \"value\"}"));

        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(1, resolvedParameters.length);
        assertEquals("value", ((SomeData) resolvedParameters[0]).getKey()); // Replace with your expected parameter value
    }

    private class SomeController {
        public String addSomeData(@LuisBody SomeData body){
            return "added";
        }

        public String withMultipleParams(@LuisBody SomeData body, @LuisPathVariable("key") String path, @LuisRequestParam("paramName") String param){
            return body.getKey() + path + param;
        }

        public String noParams(){
            return "added";
        }
    }

    private class SomeData {
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    };


    @Test
    void testResolveMethodParameters_MultipleParameterTypes() throws IOException {
        DefaultMethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = SomeController.class.getDeclaredMethods()[1];
        String methodUri = "/some/uri/{key}";

        when(mockRequest.getInputStream())
                .thenReturn(new MockServletInputStream("{\"key\": \"valueOnBody\"}"));

        when(mockRequest.getRequestURI())
                .thenReturn("/some/uri/valueOnPath");

        when(mockRequest.getParameter("paramName"))
                .thenReturn("paramValue");

        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);


        assertEquals(3, resolvedParameters.length);
        assertEquals("valueOnBody", ((SomeData) resolvedParameters[0]).getKey());
        assertEquals("valueOnPath", resolvedParameters[1]);
        assertEquals("paramValue", resolvedParameters[2]);
    }

    @Test
    void testResolveMethodWithNoParameters() {
        DefaultMethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = SomeController.class.getDeclaredMethods()[2];
        String methodUri = "/some/uri";

        Object[] resolvedParametersWithoutBody = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(0, resolvedParametersWithoutBody.length);
    }

    @Test
    void testResolveMethodParameters_MissingBody() throws IOException {
        DefaultMethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = SomeController.class.getDeclaredMethods()[1];
        String methodUri = "/some/uri/{key}";


        when(mockRequest.getInputStream())
                .thenReturn(new MockServletInputStream(""));

        // Missing body in the request
        assertThrows(EmptyRequestBodyException.class, () -> {
            resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);
        });
    }

    @Test
    void testResolveMethodParameters_MissingPathVariable() throws IOException {
        DefaultMethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = SomeController.class.getDeclaredMethods()[1];
        String methodUri = "/some/uri/{key}";
        when(mockRequest.getInputStream())
                .thenReturn(new MockServletInputStream("{\"key\": \"valueOnBody\"}"));

        when(mockRequest.getRequestURI())
                .thenReturn("/some/uri");

        // Missing path variable in the request
        assertThrows(PathVariableNotFoundException.class, () -> {
            resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);
        });
    }

    @Test
    void testResolveMethodParameters_MissingRequestParam() throws IOException {
        DefaultMethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = SomeController.class.getDeclaredMethods()[1];
        String methodUri = "/some/uri/{key}";
        when(mockRequest.getInputStream())
                .thenReturn(new MockServletInputStream("{\"key\": \"valueOnBody\"}"));
        when(mockRequest.getRequestURI())
                .thenReturn("/some/uri/pathValue");

        // Missing path variable in the request
        assertThrows(PathVariableNotFoundException.class, () -> {
            resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);
        });
    }

}
