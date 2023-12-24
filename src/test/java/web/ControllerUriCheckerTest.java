package web;

import org.example.web.ControllerUriChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerUriCheckerTest {
    ControllerUriChecker checker;

    @BeforeEach
    void setup(){
        checker = new ControllerUriChecker();
    }

    @ParameterizedTest
    @CsvSource({
            "/product/{id},              /product/1",
            "/product/{id}/price,        /product/1/price",
            "/product/{id}/price/{date}, /product/1/price/may",
            "/simple/diple,              /simple/diple",
            "/simple,                    /simple"
    })
    void shouldMatch(String methodUri, String requestUri) {
        assertTrue(checker.matches(methodUri, requestUri));
    }

    @ParameterizedTest
    @CsvSource({
            "/product/{id},              /product",
            "/product/{id},              /person/1",
            "/product/{id},              /product/1/delete",
            "/product/{id}/price/{date}, /product/1/delete",
            "/product/{id}/price/{date}, /product/1/price/june/del"
    })
    void shouldNotMatch(String methodUri, String requestUri) {
        assertFalse(checker.matches(methodUri, requestUri));
    }
}
