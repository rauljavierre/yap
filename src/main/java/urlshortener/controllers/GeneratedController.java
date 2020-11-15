package urlshortener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Endpoint(id = "generated")  // /actuator/generated | /actuator/metrics
public class GeneratedController {

    @Autowired
    private StringRedisTemplate constantsMap;

    @ReadOperation
    public HashMap<String, String> generated() {
        HashMap<String, String> response = new HashMap<>();
        response.put("URLs", constantsMap.opsForValue().get("URLs"));
        response.put("QRs", constantsMap.opsForValue().get("QRs"));
        response.put("CSVs", constantsMap.opsForValue().get("CSVs"));

        return response;
    }
}