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
