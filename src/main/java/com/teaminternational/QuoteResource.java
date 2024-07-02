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
