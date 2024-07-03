package com.teaminternational;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
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
