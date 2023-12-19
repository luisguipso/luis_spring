import org.example.datastructures.RequestControllerData;
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
        RequestControllerData data = new RequestControllerData();
        data.setUrl("/simple");

        String requestURI = "/simple";
        assertTrue(checker.matches(data.getUrl(), requestURI));
    }

    @Test
    void givenAnURIWithTwoTokensShouldMatch() {
        RequestControllerData data = new RequestControllerData();
        data.setUrl("/simple/diple");

        String requestURI = "/simple/diple";
        assertTrue(checker.matches(data.getUrl(), requestURI));
    }

    @Test
    void givenAnURIWithAParameterShouldMatch() {
        RequestControllerData data = new RequestControllerData();
        data.setUrl("/product/{id}");

        String requestURI = "/product/1";
        assertTrue(checker.matches(data.getUrl(), requestURI));
    }

    @Test
    void givenAnURIWithoutAParameterShouldNotMatch() {
        RequestControllerData data = new RequestControllerData();
        data.setUrl("/product/{id}");

        String requestURI = "/product";
        assertFalse(checker.matches(data.getUrl(), requestURI));
    }

    @Test
    void givenAnURIWithoutDiferentTokensShouldNotMatch() {
        RequestControllerData data = new RequestControllerData();
        data.setUrl("/product/{id}");

        String requestURI = "/person/1";
        assertFalse(checker.matches(data.getUrl(), requestURI));
    }

}
