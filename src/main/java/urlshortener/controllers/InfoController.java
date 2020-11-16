package urlshortener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Component
@EndpointWebExtension(endpoint = InfoEndpoint.class)
public class InfoController {

    @Autowired
    private StringRedisTemplate constantsMap;

    @ReadOperation
    public HashMap<String, String> getInfo() {
        HashMap<String, String> response = new HashMap<>();
        response.put("URLs", constantsMap.opsForValue().get("URLs"));
        response.put("QRs", constantsMap.opsForValue().get("QRs"));
        response.put("CSVs", constantsMap.opsForValue().get("CSVs"));

        // https://www.dokry.com/9118
        response.put("timestamp", ZonedDateTime.now( ZoneOffset.UTC ).format(DateTimeFormatter.ISO_INSTANT));

        return response;
    }
}