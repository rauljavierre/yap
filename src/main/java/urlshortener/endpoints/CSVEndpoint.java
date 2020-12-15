package urlshortener.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.support.WebApplicationContextUtils;
import urlshortener.MyApplicationContextAware;
import urlshortener.services.CSVService;
import urlshortener.services.QRService;
import urlshortener.services.URLService;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(value = "/csv")
@Controller
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

    Logger logger = Logger.getLogger(CSVEndpoint.class.getName());

    @OnOpen
    public void onOpen(Session session) {
        logger.log(Level.WARNING, "OnOpen: " + session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String message) throws InterruptedException {
        logger.log(Level.WARNING, "onMessage: " + message);
        CSVService csvService = (CSVService) MyApplicationContextAware.getApplicationContext().getBean("CSVService");
        RemoteEndpoint.Async remote = session.getAsyncRemote();
        try {
            remote.sendText(csvService.generateCSVLine(message));
        } catch (IllegalStateException e) {
            // If trying to write in socket in use
            // Repeat one more time (with random sleep between 1 and 2 seconds)
            int random = (int)(2 * Math.random() + 1);
            Thread.sleep(random * 1000);
            remote.sendText(csvService.generateCSVLine(message));
        }
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

