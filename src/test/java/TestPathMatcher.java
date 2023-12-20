import org.example.web.ControllerUriChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPathMatcher {
    ControllerUriChecker checker;

    @BeforeEach
    void setup(){
        checker = new ControllerUriChecker();
    }

    @Test
    void givenASimpleUriShouldMatch() {
        assertTrue(checker.matches("/simple", "/simple"));
    }

    @Test
    void givenAnURIWithTwoTokensShouldMatch() {
        assertTrue(checker.matches("/simple/diple", "/simple/diple"));
    }

    @Test
    void givenAnURIWithAParameterShouldMatch() {
        assertTrue(checker.matches("/product/{id}", "/product/1"));
    }

    @Test
    void givenAnURIWithoutAParameterShouldNotMatch() {
        assertFalse(checker.matches("/product/{id}", "/product"));
    }

    @Test
    void givenAnURIWithoutDiferentTokensShouldNotMatch() {
        assertFalse(checker.matches("/product/{id}", "/person/1"));
    }

}
