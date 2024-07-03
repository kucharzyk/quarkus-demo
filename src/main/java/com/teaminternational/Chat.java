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
