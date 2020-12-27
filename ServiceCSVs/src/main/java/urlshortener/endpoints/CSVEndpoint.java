package urlshortener.endpoints;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import urlshortener.MyApplicationContextAware;
import urlshortener.services.CSVService;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

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

        Message m = new Message(message.getBytes(), new MessageProperties());
        RabbitTemplate rabbitTemplate = (RabbitTemplate) MyApplicationContextAware.getApplicationContext().getBean("rabbitTemplate");
        String response = "pre";
        try {
            response = rabbitTemplate.sendAndReceive("", "yap.request", m).toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Exception:" + e.toString());
        }
        logger.log(Level.WARNING, "Response: " + response);

        synchronized (clients) {
            for(Session client : clients){
                if (client.equals(session)){
                    client.getBasicRemote().sendText(response);
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
