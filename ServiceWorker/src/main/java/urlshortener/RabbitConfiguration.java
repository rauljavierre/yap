package urlshortener;

import org.json.simple.parser.ParseException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import urlshortener.services.CSVService;
import java.io.IOException;
import java.util.logging.Logger;

@Configuration
public class RabbitConfiguration {

    public static final String QUEUE_NAME = "yap.request";

    private static final String EXCHANGE_NAME = "";

    @Autowired
    CSVService csvService;

    Logger logger = Logger.getLogger(RabbitConfiguration.class.getName());

    @Bean
    public ConnectionFactory connectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory("rabbitmq");
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @RabbitListener(queues = QUEUE_NAME, concurrency = "16")
    public void onMessageFromRabbitMQ(final String messageFromRabbitMQ) throws IOException, ParseException {
        logger.info(messageFromRabbitMQ);
        String url = messageFromRabbitMQ.split(";")[0];
        String queue = messageFromRabbitMQ.split(";")[1];
        Message response = new Message(csvService.generateCSVLine(url).getBytes(), new MessageProperties());
        this.rabbitTemplate().send(queue, response);
    }
}