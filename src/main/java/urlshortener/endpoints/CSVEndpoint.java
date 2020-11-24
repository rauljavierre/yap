package urlshortener.endpoints;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import urlshortener.services.CSVService;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(value = "/csv")
@Component
public class CSVEndpoint {

    @Autowired
    CSVService csvService;

    Logger logger = Logger.getLogger(CSVEndpoint.class.getName());

    @OnOpen
    public void onOpen(Session session) {
        logger.log(Level.WARNING, "OnOpen: " + session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        logger.log(Level.WARNING, "OnMessage: " + session.getId());
        logger.log(Level.WARNING, "csvService: " + csvService); // null
        session.getAsyncRemote().sendText(csvService.generateCSVLine(message));
    }

    @OnClose
    public void onClose(Session session) {
        logger.log(Level.WARNING, "OnClose: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.log(Level.WARNING, "OnError: " + throwable.getMessage());
    }
}

