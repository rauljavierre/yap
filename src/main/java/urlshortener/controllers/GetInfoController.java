package urlshortener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.util.*;


@Controller
@EnableSwagger2
public class GetInfoController {

    @Autowired
    private StringRedisTemplate constantsMap;

    @GetMapping("/get_info")
    public ResponseEntity<HashMap<String, String>> getInfo() {
        HashMap<String, String> info = new HashMap<>();
        // TODO: prometheus/grafana || JSON con información
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}