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
