package urlshortener.controllers;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Channel;


@Controller
@EnableSwagger2
public class GetInfoController {

    private Map<String, String> lastAvailableMap = new HashMap<>();
    private Map<String, Channel> channelMap = null;
    private List<String> informationKeys = Arrays.asList(
            "total-memory-queue", "used-memory-queue",
            "available-memory-queue", "platform-queue", "cpu-used-queue",
            "cpu-cores-queue", "cpu-frequency-queue", "boot-time-queue");

    @Autowired
    private StringRedisTemplate constantsMap;

    @GetMapping("/get_info")
    public ResponseEntity<HashMap<String, String>> getInfo() {
        HashMap<String, String> info = new HashMap<>();

        info.put("NumberOfGeneratedURLs",
                (constantsMap.opsForValue().get("URLs") == null) ? "0" : constantsMap.opsForValue().get("URLs"));
        info.put("NumberOfGeneratedQRs",
                (constantsMap.opsForValue().get("QRs") == null) ? "0" : constantsMap.opsForValue().get("QRs"));
        info.put("NumberOfGeneratedCSVs",
                (constantsMap.opsForValue().get("CSVs") == null) ? "0" : constantsMap.opsForValue().get("CSVs"));
        info.put("TotalMemory", readFromRabbitMQ("total-memory-queue"));
        info.put("UsedMemory", readFromRabbitMQ("used-memory-queue"));
        info.put("AvailableMemory", readFromRabbitMQ("available-memory-queue"));
        info.put("Platform", readFromRabbitMQ("platform-queue"));
        info.put("UsageOfCPU", readFromRabbitMQ("cpu-used-queue"));
        info.put("NumberOfCores", readFromRabbitMQ("cpu-cores-queue"));
        info.put("CPUFrequency", readFromRabbitMQ("cpu-frequency-queue"));
        info.put("BootTime", readFromRabbitMQ("boot-time-queue"));

        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    private String readFromRabbitMQ(String key) {
        initialize();   // initialize connections with rabbitmq if needed
        try {
            String info = new String(channelMap.get(key).basicGet(key, false).getBody(), StandardCharsets.UTF_8);
            lastAvailableMap.put(key, info);
            return info;
        }
        catch (NullPointerException | IOException e){   // No updates | Connection Problems
            return lastAvailableMap.get(key);
        }
    }

    private void initialize() {
        if(channelMap == null) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq");
            try {
                Connection connection = factory.newConnection();
                channelMap = new HashMap<>();
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