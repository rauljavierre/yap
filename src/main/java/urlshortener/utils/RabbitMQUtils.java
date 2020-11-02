package urlshortener.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


public class RabbitMQUtils {

    public static String readFromRabbitMQ(Map<String, Channel> channelMap, Map<String, String> lastAvailableMap, String key) {
        try {
            String info = new String(channelMap.get(key).basicGet(key, false).getBody(), StandardCharsets.UTF_8);
            lastAvailableMap.put(key, info);
            return info;
        }
        catch (NullPointerException | IOException e){   // No updates | Connection Problems
            return lastAvailableMap.get(key);
        }
    }

    public static String getNumberOf(StringRedisTemplate constantsMap, String param) {
        return constantsMap.opsForValue().get(param);
    }

    public static void initialize(Map<String, Channel> channelMap,List<String> informationKeys) {
        if(channelMap == null) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq");
            try {
                channelMap = new HashMap<>();
                Connection connection = factory.newConnection();
                Map<String, Object> args = new HashMap<>();
                args.put("x-max-length", 1);
                for (String key : informationKeys) {
                    Channel c = connection.createChannel();
                    channelMap.put(key, c);
                    c.queueDeclare(key, false, false, false, args);
                }
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
}