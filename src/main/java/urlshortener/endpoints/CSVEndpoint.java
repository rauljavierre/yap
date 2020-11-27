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
    public void onMessage(Session session, String message) {
        logger.log(Level.WARNING, "onMessage: " + message);
        Thread thread = new Thread() {
            public void run(){
                CSVService csvService = (CSVService) MyApplicationContextAware.getApplicationContext().getBean("CSVService");
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

