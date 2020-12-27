package urlshortener;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Logger;

@Configuration
public class RabbitConfiguration {

    public static final String QUEUE_NAME = "yap.request";

    private static final String EXCHANGE_NAME = "";

    Logger logger = Logger.getLogger(RabbitConfiguration.class.getName());

    @Bean
    public ConnectionFactory connectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory("rabbitmq");
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setRoutingKey(QUEUE_NAME);
        return rabbitTemplate;
    }

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(QUEUE_NAME);
    }

    @RabbitListener(queues = RabbitConfiguration.QUEUE_NAME)
    public void onMessageFromRabbitMQ(final String messageFromRabbitMQ){
        logger.info(messageFromRabbitMQ);
        String queue = messageFromRabbitMQ.split(";")[1];
        Message response = new Message("Holaaaaaaaa".getBytes(), new MessageProperties());
        this.rabbitTemplate().send(queue, response);
    }
}