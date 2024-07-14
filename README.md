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

## step 6 (native container build - optional)

* update ```pom.xml```

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-container-image-docker</artifactId>
</dependency>
```

* build image

```bash
./mvnw clean install -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
```

* run image (note: we are overriding database url to access localhost from docker container)

```bash
docker run -it --rm -p 8080:8080 -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host.docker.internal:5432/quarkus tomasz/quarkus-demo:1.0.0-SNAPSHOT
```

## step 7

* install Ollama and Llama3 [https://ollama.com](https://ollama.com)

* add langchain4j ollama integration

```xml
        <dependency>
            <groupId>io.quarkiverse.langchain4j</groupId>
            <artifactId>quarkus-langchain4j-ollama</artifactId>
            <version>0.15.1</version>
        </dependency>
```

* create AI

```java
package com.teaminternational;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.SessionScoped;

@RegisterAiService
@SessionScoped
public interface Hal9000 {

    @UserMessage("""
             Dear Sir/Madam,
            
             Please return random quote.
             Respond in JSON format only by providing object with fields author and quote.
             Do not add anything else except json to response!
            
             Thanks.
            """)
    Quote getRandomQuote();
}

```

* use AI in ```QuoteService```

```java
package com.teaminternational;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final Hal9000 hal9000;

    public QuoteService(QuoteRepository quoteRepository, Hal9000 hal9000) {
        this.quoteRepository = quoteRepository;
        this.hal9000 = hal9000;
    }

    @Transactional
    public Quote getRandomQuote() {
        Quote aiQuote = hal9000.getRandomQuote();

        Quote quote = new Quote(aiQuote.getAuthor(), aiQuote.getQuote());
        quoteRepository.persist(quote);
        return quote;
    }
    public List<Quote> getAllQuotes() {
        return quoteRepository.listAll();
    }
}
```

## step 8

* add quote

```xml
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-qute</artifactId>
        </dependency>
```

* create chat resource

```java
package com.teaminternational;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("")
public class Chat {

    private final Template chat;
    
    public Chat(Template chat) {
        this.chat = chat;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getChatPage() {
        return chat.instance();
    }
}
```

* create template in ```resources/templates/chat.html```

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>HAL 9000</title>
  <script src="https://unpkg.com/htmx.org@2.0.0"></script>
  <script src="https://unpkg.com/htmx-ext-ws@2.0.0/ws.js"></script>
</head>
<body style="display: flex; flex-direction: column; align-items: center;">
<h1>HAL 9000</h1>
<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/HAL9000.svg/256px-HAL9000.svg.png" alt="AI logo" style="margin: 2em;">
<div hx-ext="ws" ws-connect="/ai-ws" class="chat">
  <div id="notifications" ></div>
  <form ws-send hx-on:submit="htmx.find('#message').value=''">
    <input name="message" id="message" autofocus style="width: 90vw;">
  </form>
</div>
</body>
</html>
```


## step 9

* remove ```@SessionScoped``` from ```Hal9000``` to fix test

* create chat message object

```java
package com.teaminternational;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ChatMessage(String message, @JsonProperty("HEADERS") Map<String, Object> headers) {
}

```

* add websockets

```xml
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-websockets-next</artifactId>
        </dependency>
```

* create ```Hal900Chat```

```java

package com.teaminternational;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;

@RegisterAiService
@SessionScoped
public interface Hal9000Chat {

    @SystemMessage("Please pretend to be evil robot called HAL 9000.")
    Multi<String> ask(
            @UserMessage String message
    );
}

```

* implement websocket 

```java
package com.teaminternational;

import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;

@WebSocket(path = "/ai-ws")
public class AiWebSocket {

    @Inject
    Hal9000Chat hal9000Chat;

    @OnOpen
    public Multi<String> onOpen() {
        return Multi.createBy().concatenating().streams(
                Multi.createFrom().item(wrapAsHTMXResponse("<b>AI: </b>")),
                hal9000Chat.ask("Say hello to me and ask if I need help").map(this::wrapAsHTMXResponse),
                Multi.createFrom().item(wrapAsHTMXResponse("<br/><br/>"))
        );
    }

    @OnTextMessage
    public Multi<String> onMessage(ChatMessage message) {
        if (message.message() == null || message.message().isEmpty()) {
            return Multi.createFrom().empty();
        }

        Log.info(message);
        return Multi.createBy().concatenating().streams(
                Multi.createFrom().item(wrapAsHTMXResponse("<b>You: </b>" + message.message() + "<br/></br>")),
                Multi.createFrom().item(wrapAsHTMXResponse("<b>AI: </b>")),
                hal9000Chat.ask(message.message()).map(this::wrapAsHTMXResponse),
                Multi.createFrom().item(wrapAsHTMXResponse("<br/><br/>"))
        );

    }

    private String wrapAsHTMXResponse(String it) {
        return "<div id=\"notifications\" hx-swap-oob=\"beforeend\"><span>" + it + "</span></div>";
    }
}


```

* add style to notifications

```html
<div id="notifications" style="max-width: 90vw"></div>
```
## step 10 (native container build)

* register ```ChatMessage``` for reflection

```java
package com.teaminternational;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

@RegisterForReflection
public record ChatMessage(String message, @JsonProperty("HEADERS") Map<String, Object> headers) {
}

```

* build image

```bash
./mvnw clean install -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
```

* run image (note: we are overriding database url to access localhost from docker container)

```bash
docker run -it --rm -p 8080:8080 -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host.docker.internal:5432/quarkus -e QUARKUS_LANGCHAIN4J_OLLAMA_BASE_URL=http://host.docker.internal:11434 tomasz/quarkus-demo:1.0.0-SNAPSHOT
```

## NOTES

There is an issue with current version of langchain4j and latest ollama (2.0.5)

https://github.com/langchain4j/langchain4j/issues/1461

It's working fine with 0.1.45
