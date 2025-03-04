package com.teaminternational;

import jakarta.persistence.*;

@Entity
@Table(name = "quotes")
public class Quote {
    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "text")
    private String author;
    @Column(columnDefinition = "text")
    private String quote;

    protected Quote() {
    }

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
