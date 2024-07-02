package com.teaminternational;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
class QuoteResourceTest {

    @Test
    void getRandomQuote() {
        RestAssured.given()
                .get("/quotes/random")
                .then()
                .assertThat()
                .statusCode(200)
                .body("id", Matchers.notNullValue())
                .body("author", Matchers.notNullValue())
                .body("quote", Matchers.notNullValue());
    }
}