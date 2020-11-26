package urlshortener.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import urlshortener.MyApplicationContextAware;
import urlshortener.services.CSVService;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(value = "/csv")
@Component
public class CSVEndpoint {

    /*
    InputMessage:
        - host -> http://yapsh.tk/ | http://localhost/
        - url -> http://airezico.tk

    OutputMessage -> // Quizás cambiar a string porque el frontend debería deserializar y pasa
        - long_url -> http://airezico.tk
        - short_url -> http://.../hash
        - error ->
     */

    @Autowired
    CSVService csvService = (CSVService) MyApplicationContextAware.getApplicationContext().getBean("CSVService");

    Logger logger = Logger.getLogger(CSVEndpoint.class.getName());

    @OnOpen
    public void onOpen(Session session) {
        logger.log(Level.WARNING, "OnOpen: " + session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        logger.log(Level.WARNING, "onMessage: " + message);
        Thread thread = new Thread() {
            public void run(){
                session.getAsyncRemote().sendText(csvService.generateCSVLine(message));
            }
        };
        thread.start();
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

