package urlshortener.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.support.WebApplicationContextUtils;
import urlshortener.MyApplicationContextAware;
import urlshortener.services.CSVService;
import urlshortener.services.QRService;
import urlshortener.services.URLService;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(value = "/csv")
@Controller
public class CSVEndpoint {

    // https://www.byteslounge.com/tutorials/java-ee-html5-websockets-with-multiple-clients-example
    private static Set<Session> clients = Collections.synchronizedSet(new HashSet<>());

    Logger logger = Logger.getLogger(CSVEndpoint.class.getName());

    @OnOpen
    public void onOpen(Session session) {
        logger.log(Level.WARNING, "OnOpen: " + session.getId());
        clients.add(session);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        logger.log(Level.WARNING, "onMessage: " + message);
        CSVService csvService = (CSVService) MyApplicationContextAware.getApplicationContext().getBean("CSVService");
        String response = csvService.generateCSVLine(message);
        synchronized (clients) {
            for(Session client : clients){
                if (client.equals(session)){
                    client.getBasicRemote().sendText(response);
                    logger.log(Level.WARNING, "onResponse: " + response);
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        CSVService csvService = (CSVService) MyApplicationContextAware.getApplicationContext().getBean("CSVService");
        csvService.incrementNumberOfCSVs();
        logger.log(Level.WARNING, "OnClose: " + session.getId());
        clients.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.log(Level.WARNING, "OnError: " + throwable.getMessage());
        clients.remove(session);
    }
}
