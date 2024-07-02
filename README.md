# quarkus-demo

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## step 1

* visit [https://code.quarkus.io/](https://code.quarkus.io/)
* set group: ```com.teaminternational```
* set artifact name: ```quarkus-demo```
* set Java version: ```21```
* set build tool: ```Maven```
* set starter code: ```No```
* add extensions: ```quarkus-rest```, ```quarkus-rest-jackson```, ```quarkus-smallrye-openapi```
* run ```./mvnw quarkus:dev``` to start DEV mode
* open [http://localhost:8080/q/dev-ui/welcome](http://localhost:8080/q/dev-ui/welcome)

## step 2
* create ```java``` directory inside ```src/main```
* create ```com.teaminternational``` package
* create `Quote` class

```java
package com.teaminternational;

public class Quote {
    private Long id;
    private String author;
    private String quote;

    public Quote(String author, String quote) {
        this.author = author;
        this.quote = quote;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "id=" + id +
                ", author='" + author + '\'' +
                ", quote='" + quote + '\'' +
                '}';
    }
}

```

* create `QuoteResource` class

```java
package com.teaminternational;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.UUID;

@Path("/quotes")
public class QuoteResource {
    
    @GET
    @Path("/random")
    public Quote getRandomQuote() {
        return new Quote(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }
    
}

```
* create new HTTP Request file ```requests.http``` for testing

```md
### GET random quote
GET http://localhost:8080/quotes/random
Content-Type: */*

###
```

* add restassured dependency

```xml
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>
```

* generate test for ```QuoteResource```

```java
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
                .body("id", Matchers.nullValue())
                .body("author", Matchers.notNullValue())
                .body("quote", Matchers.notNullValue());
    }
}
```

## step 3

* introduce ```QuoteService```

```java
package com.teaminternational;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class QuoteService {

    public Quote getRandomQuote() {
        return new Quote(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

}
```

* inject service to quote resource

```java
package com.teaminternational;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/quotes")
public class QuoteResource {

    private final QuoteService quoteService;

    public QuoteResource(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GET
    @Path("/random")
    public Quote getRandomQuote() {
        return quoteService.getRandomQuote();
    }

}

```

* add database dependencies

```xml
       <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-validator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>
```

* modify `Quote` to make it entity

```java
package com.teaminternational;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quotes")
public class Quote {
    @Id
    @GeneratedValue
    private Long id;
    private String author;
    private String quote;

    protected Quote() {
    }
    
    ///...
}
```

* create ```QuoteRepository```

```java
package com.teaminternational;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

public interface QuoteRepository extends PanacheRepository<Quote> {
}
```

* update ```QuoteService```

```java
package com.teaminternational;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class QuoteService {

    private final QuoteRepository quoteRepository;

    public QuoteService(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public Quote getRandomQuote() {
        Quote quote = new Quote(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        quoteRepository.persist(quote);
        return quote;
    }

}

```

* fix broken test

```java
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
```