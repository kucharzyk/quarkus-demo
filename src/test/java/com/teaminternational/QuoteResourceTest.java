package com.teaminternational;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
class QuoteResourceTest {

    private final QuoteRepository quoteRepository;

    public QuoteResourceTest(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Test
    void getAllQuotes() {
        RestAssured.given()
                .get("/quotes")
                .then()
                .assertThat()
                .statusCode(200)
                .body("$", Matchers.hasSize((int) quoteRepository.count()));
    }

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