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
