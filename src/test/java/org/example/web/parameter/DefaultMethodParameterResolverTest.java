package org.example.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.example.web.parameter.mock.MockController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultMethodParameterResolverTest {

    private PathVariableResolver pathResolverMock;
    private RequestParameterResolver paramResolverMock;
    private RequestBodyResolver bodyResolverMock;
    private MethodParameterResolver resolver;

    @BeforeEach
    void setup(){
        pathResolverMock = mock(PathVariableResolver.class);
        paramResolverMock = mock(RequestParameterResolver.class);
        bodyResolverMock = mock(RequestBodyResolver.class);
        List<ArgumentResolverTemplate> parameterResolvers = List.of(
                pathResolverMock,
                paramResolverMock,
                bodyResolverMock);
        resolver = new DefaultMethodParameterResolver(parameterResolvers);
    }

    @Test
    void testResolveMethodParameters_MultipleParameterTypes() {
        when(pathResolverMock.resolveMethodParameters(any(), any(), anyString()))
                .thenReturn(List.of("valueOnPath"));
        when(paramResolverMock.resolveMethodParameters(any(), any(), anyString()))
                .thenReturn(List.of("paramValue"));
        when(bodyResolverMock.resolveMethodParameters(any(), any(), anyString()))
                .thenReturn(List.of(new MockController.SomeData().setKey("valueOnBody")));

        Object[] resolvedParameters = resolver.resolveMethodParameters(mock(HttpServletRequest.class), mock(Method.class), "");

        assertEquals(3, resolvedParameters.length);
        assertEquals("valueOnPath", resolvedParameters[0]);
        assertEquals("paramValue", resolvedParameters[1]);
        assertEquals("valueOnBody", ((MockController.SomeData) resolvedParameters[2]).getKey());
    }

    @Test
    void testResolveMethodWithNoParameters() {

        Object[] resolvedParametersWithoutBody = resolver.resolveMethodParameters(any(), any(), anyString());

        assertEquals(0, resolvedParametersWithoutBody.length);
    }

    public static Method getMethodOfClassWithName(Class<MockController> clazz, String methodName) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
    }
}
