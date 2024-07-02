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

## step 4

* add getAllQuotes method in ```QuoteService```

```java
    public List<Quote> getAllQuotes() {
        return quoteRepository.listAll();
    }
```

* update resource

```java
    @GET
    public List<Quote> getAllQuotes() {
        return quoteService.getAllQuotes();
    }
```

* add test

```java
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
```

## step 5

* create local database using docker

```bash
docker run --name quarkus-demo-postgres -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=mysecretpassword -e POSTGRES_DB=quarkus -d -p5432:5432 postgres 
 
docker start quarkus-demo-postgres
```

* add database config to ```application.properties```

```properties
quarkus.datasource.db-kind=postgresql
quarkus.hibernate-orm.physical-naming-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
quarkus.hibernate-orm.database.generation=update

%prod.quarkus.datasource.username=quarkus
%prod.quarkus.datasource.password=mysecretpassword
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://127.0.0.1:5432/quarkus

%dev.quarkus.datasource.username=quarkus
%dev.quarkus.datasource.password=mysecretpassword
%dev.quarkus.datasource.jdbc.url=jdbc:postgresql://127.0.0.1:5432/quarkus

%dev.quarkus.hibernate-orm.log.bind-parameters=true
%dev.quarkus.hibernate-orm.log.sql=true
```

* update entity to make sure we can handle long text

```java
    @Column(columnDefinition = "text")
    private String author;
    @Column(columnDefinition = "text")
    private String quote;
```

* build service and start it

```bash
./mvnw clean verify
java -jar ./target/quarkus-app/quarkus-run.jar
```