package urlshortener.controllers;

import urlshortener.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.util.*;
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
        RabbitMQUtils.initialize(this.channelMap,this.informationKeys);   // initialize connections with rabbitmq if needed

        info.put("NumberOfGeneratedURLs", RabbitMQUtils.getNumberOf(constantsMap, "URLs"));
        info.put("NumberOfGeneratedQRs", RabbitMQUtils.getNumberOf(constantsMap, "QRs"));
        info.put("NumberOfGeneratedCSVs", RabbitMQUtils.getNumberOf(constantsMap, "CSVs"));
        info.put("TotalMemory",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "total-memory-queue"));
        info.put("UsedMemory",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "used-memory-queue"));
        info.put("AvailableMemory",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "available-memory-queue"));
        info.put("Platform",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "platform-queue"));
        info.put("UsageOfCPU",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "cpu-used-queue"));
        info.put("NumberOfCores",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "cpu-cores-queue"));
        info.put("CPUFrequency",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "cpu-frequency-queue"));
        info.put("BootTime", RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "boot-time-queue"));
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

}