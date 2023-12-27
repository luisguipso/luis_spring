package web;

import jakarta.servlet.http.HttpServletRequest;
import org.example.datastructures.ControllersMap;
import org.example.datastructures.RequestControllerData;
import org.example.web.DefaultControllerResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultControllerResolverTest {

    private DefaultControllerResolver controllerResolver;

    @BeforeEach
    void setup() {
        insertRequestControllerDataOnInMemoryMap("GET", "/example", "handleGetRequest");
        insertRequestControllerDataOnInMemoryMap("POST", "/example", "handlePostRequest");
        insertRequestControllerDataOnInMemoryMap("GET", "/test/{id}", "handleGetWithId");
        insertRequestControllerDataOnInMemoryMap("GET", "/test/{id}/additional", "handleGetWithIdExtraPathSegment");
        controllerResolver = new DefaultControllerResolver();
    }

    private static void insertRequestControllerDataOnInMemoryMap(String method, String url, String methodName) {
        RequestControllerData mockControllerData = new RequestControllerData(method, url, "anyclass", methodName);
        ControllersMap.values.put(mockControllerData.getHttpMethod() + mockControllerData.getUrl(), mockControllerData);
    }

    @Test
    void testSearchController_DirectMatch() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/example");
        when(mockRequest.getMethod()).thenReturn("GET");
        
        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals("handleGetRequest", result.orElseThrow().getControllerMethod());
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

        assertEquals(method, result.orElseThrow().getControllerMethod());
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

        assertEquals("handleGetWithId", result.orElseThrow().getControllerMethod());
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
        assertEquals("handleAnotherGetRequest", result.orElseThrow().getControllerMethod());
    }

    @Test
    void testSearchController_ParameterizedPath() {
        // Test parameterized path matching
        insertRequestControllerDataOnInMemoryMap("POST","/users/{userId}/posts/{postId}", "handleUserPostRequest");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/users/123/posts/456");
        when(mockRequest.getMethod()).thenReturn("POST");

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals("handleUserPostRequest", result.orElseThrow().getControllerMethod());
    }
    @Test
    void testSearchController_SpecialCharactersInURI() {
        // Test URIs with special characters
        insertRequestControllerDataOnInMemoryMap("GET","/items/{itemId}/details", "handleItemDetailsRequest");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/items/abc%2F123/details"); // URI-encoded special character "/"
        when(mockRequest.getMethod()).thenReturn("GET");

        Optional<RequestControllerData> result = controllerResolver.findController(mockRequest);

        assertEquals("handleItemDetailsRequest", result.orElseThrow().getControllerMethod());
    }

}