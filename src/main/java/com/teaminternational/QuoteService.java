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
