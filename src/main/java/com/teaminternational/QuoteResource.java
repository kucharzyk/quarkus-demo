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
