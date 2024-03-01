package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.example.annotation.LuisBody;
import org.example.annotation.LuisPathVariable;
import org.example.annotation.LuisRequestParam;
import org.example.web.MockServletInputStream;
import org.example.web.exception.PathVariableNotFoundException;
import org.example.web.exception.RequestBodyNotFoundException;
import org.example.web.exception.RequestParamNotFoundException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultMethodParameterResolverTest {

    @Test
    void testResolveMethodParameters_LuisBody() throws IOException {
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class, "methodWithBody");
        String methodUri = "/some/uri";

        when(mockRequest.getInputStream()).thenReturn(new MockServletInputStream("{\"key\": \"value\"}"));

        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(1, resolvedParameters.length);
        assertEquals("value", ((SomeData) resolvedParameters[0]).getKey());
    }

    @Test
    void testResolveMethodParameters_PathVariable() {
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class,"methodWithPathVariable");
        String methodUri = "/some/uri/{key}";

        when(mockRequest.getRequestURI())
                .thenReturn("/some/uri/valueOnPath");


        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);


        assertEquals(1, resolvedParameters.length);
        assertEquals("valueOnPath", resolvedParameters[0]);
    }

    @Test
    void testResolveMethodParameters_WithTwoPathVariables() {
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class,"methodWithTwoPathVariables");
        String methodUri = "/some/uri/{key}/lock/{lock}";

        when(mockRequest.getRequestURI())
                .thenReturn("/some/uri/Doorskey/lock/Windowslock");


        Object[] resolvedParameters = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(2, resolvedParameters.length);
        assertEquals("Doorskey", resolvedParameters[0]);
        assertEquals("Windowslock", resolvedParameters[1]);
    }

    @Test
    void testResolveMethodParameters_RequestParam() {
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
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
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
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
    void testResolveMethodParameters_MultipleParameterTypes() throws IOException {
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class,"withMultipleParams");
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
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class, "methodWithNoParams");
        String methodUri = "/some/uri";

        Object[] resolvedParametersWithoutBody = resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri);

        assertEquals(0, resolvedParametersWithoutBody.length);
    }

    @Test
    void testResolveMethodParameters_MissingBody() throws IOException {
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class, "withMultipleParams");
        String methodUri = "/some/uri/{key}";

        when(mockRequest.getInputStream())
                .thenReturn(new MockServletInputStream(""));

        // Missing body in the request
        assertThrows(RequestBodyNotFoundException.class, () ->
                resolver.resolveMethodParameters(mockRequest, mockMethod, methodUri)
        );
    }

    @Test
    void testResolveMethodParameters_MissingPathVariable() throws IOException {
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Method mockMethod = getMethodOfClassWithName(SomeController.class, "methodWithPathVariable");
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
    void testResolveMethodParameters_MissingRequestParam() throws IOException {
        MethodParameterResolver resolver = new DefaultMethodParameterResolver();
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

    private Method getMethodOfClassWithName(Class<SomeController> clazz, String methodName) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private class SomeController {
        public String methodWithBody(@LuisBody SomeData body){
            return "added";
        }

        public String withMultipleParams(@LuisBody SomeData body, @LuisPathVariable("key") String path, @LuisRequestParam("paramName") String param){
            return body.getKey() + path + param;
        }

        public String methodWithPathVariable(@LuisPathVariable("key") String path){
            return path;
        }

        public String methodWithTwoPathVariables(@LuisPathVariable("key") String key, @LuisPathVariable("lock") String lock){
            return "key: " + key + " lock: " + lock;
        }

        public String methodWithRequestParam(@LuisRequestParam("paramName") String param){
            return param;
        }

        public String methodWithTwoRequestParam(
                @LuisRequestParam("paramName") String param,
                @LuisRequestParam("anotherParam") String another
        ){
            return param + another;
        }

        public String methodWithNoParams(){
            return "added";
        }
    }

    private static class SomeData {
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

}
