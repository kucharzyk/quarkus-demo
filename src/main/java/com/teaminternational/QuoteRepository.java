package com.teaminternational;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QuoteRepository implements PanacheRepository<Quote> {
}
