package org.example.web;

import org.example.datastructures.ComponentsInstances;
import org.example.util.LuisLogger;
import org.example.web.exception.MethodNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

class DefaultControllerInstanceResolverTest {

    private DefaultControllerInstanceResolver resolver;


    @BeforeEach
    void setup() {
        resolver = new DefaultControllerInstanceResolver();
    }

    @Test
    void testGetController_ExistingController() {
        // Mocking dependencies
        String controllerClassName = "ExistingController";
        Object mockController = Mockito.mock(Object.class);
        ComponentsInstances.instances.put(controllerClassName, mockController);

        try (MockedStatic<LuisLogger> mockedLogger = Mockito.mockStatic(LuisLogger.class)) {

            Object controller = resolver.getController(controllerClassName);

            assertNotNull(controller);
            assertEquals(mockController, controller);
            mockedLogger.verify(() -> LuisLogger.log(eq(DefaultControllerInstanceResolver.class), eq("Searching for controller instance")));
        }
    }

    @Test
    void testGetController_NonExistingController() {
        // Mocking dependencies
        String controllerClassName = "NonExistingController";

        try (MockedStatic<LuisLogger> mockedLogger = Mockito.mockStatic(LuisLogger.class)) {

            MethodNotFoundException exception = assertThrows(MethodNotFoundException.class,
                    () -> resolver.getController(controllerClassName));

            assertEquals("Controller not found", exception.getMessage());
            mockedLogger.verify(() -> LuisLogger.log(eq(DefaultControllerInstanceResolver.class), eq("Searching for controller instance")));
        }
    }

    @Test
    void testGetController_EmptyClassName() {
        assertThrows(MethodNotFoundException.class, () -> resolver.getController(""));
        assertThrows(MethodNotFoundException.class, () -> resolver.getController(null));
    }
}
