package urlshortener;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void receiveMessage(String message) {
        LOGGER.info("Received " + message);
        String queue = message.split(";")[1];
        rabbitTemplate.convertAndSend(queue, "Hey crack");
    }
}
