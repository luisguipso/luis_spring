package org.example.web;

import jakarta.servlet.http.HttpServletRequest;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.web.DefaultControllerDataResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultControllerDataResolverTest {

    private DefaultControllerDataResolver controllerResolver;

    @BeforeEach
    void setup() {
        insertRequestControllerDataOnInMemoryMap("GET", "/example", "handleGetRequest");
        insertRequestControllerDataOnInMemoryMap("POST", "/example", "handlePostRequest");
        insertRequestControllerDataOnInMemoryMap("GET", "/test/{id}", "handleGetWithId");
        insertRequestControllerDataOnInMemoryMap("GET", "/test/{id}/additional", "handleGetWithIdExtraPathSegment");
        controllerResolver = new DefaultControllerDataResolver();
    }

    private static void insertRequestControllerDataOnInMemoryMap(String method, String url, String methodName) {
        insertRequestControllerDataOnInMemoryMap(method, url, methodName, 0);
    }

    private static void insertRequestControllerDataOnInMemoryMap(String method, String url, String methodName, long numberOfParams) {
        RequestControllerData mockControllerData = new RequestControllerData(method, url, "anyclass", methodName, mock(Method.class));
        String key = mockControllerData.getHttpMethod() + mockControllerData.getUrl() + "/p=" + numberOfParams;
        ControllersMap.values.put(key, mockControllerData);
    }

    @Test
    void testSearchController_DirectMatch() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/example");
        when(mockRequest.getMethod()).thenReturn("GET");
        
        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals("handleGetRequest", result.orElseThrow().getMethodName());
    }

    @ParameterizedTest
    @CsvSource({
            "/example, GET, handleGetRequest",
            "/test/1, GET, handleGetWithId",
            "/example, POST, handlePostRequest",
            "/test/1/additional, GET, handleGetWithIdExtraPathSegment"
    })
    void testSearchController_DirectMatch(String uri, String httpMethod, String method) {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn(uri);
        when(mockRequest.getMethod()).thenReturn(httpMethod);

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals(method, result.orElseThrow().getMethodName());
    }

    @Test
    void testSearchController_NoMatch() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/test/1");
        when(mockRequest.getMethod()).thenReturn("GET");
        ControllersMap.values.clear(); // Clearing the map for this test

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testSearchController_ExactMatchDifferentMethodThatNotExists() {
        // Exact URI match but different method
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/example");
        when(mockRequest.getMethod()).thenReturn("PUT");

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testSearchController_InvalidRequest() {
        // Test with null request
        Optional<RequestControllerData> result = controllerResolver.findController(null);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testSearchController_CaseSensitiveURI() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/Example");
        when(mockRequest.getMethod()).thenReturn("GET");

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testSearchController_URIVariations() {
        // Test URI variations (with and without trailing slash)
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/test/1/");
        when(mockRequest.getMethod()).thenReturn("GET");

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertFalse(result.isPresent());
    }

    @Test
    void testSearchController_MultipleControllersForSameURI() {
        insertRequestControllerDataOnInMemoryMap("GET","/example", "handleGetRequest");
        insertRequestControllerDataOnInMemoryMap("GET", "/example", "handleAnotherGetRequest");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/example");
        when(mockRequest.getMethod()).thenReturn("GET");

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertTrue(result.isPresent());
        assertEquals("handleAnotherGetRequest", result.orElseThrow().getMethodName());
    }

    @Test
    void testSearchController_ParameterizedPath() {
        // Test parameterized path matching
        insertRequestControllerDataOnInMemoryMap("POST","/users/{userId}/posts/{postId}", "handleUserPostRequest");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/users/123/posts/456");
        when(mockRequest.getMethod()).thenReturn("POST");

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals("handleUserPostRequest", result.orElseThrow().getMethodName());
    }
    @Test
    void testSearchController_SpecialCharactersInURI() {
        // Test URIs with special characters
        insertRequestControllerDataOnInMemoryMap("GET","/items/{itemId}/details", "handleItemDetailsRequest");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/items/abc%2F123/details"); // URI-encoded special character "/"
        when(mockRequest.getMethod()).thenReturn("GET");

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals("handleItemDetailsRequest", result.orElseThrow().getMethodName());
    }

}