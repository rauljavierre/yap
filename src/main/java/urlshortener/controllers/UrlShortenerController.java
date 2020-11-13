package urlshortener.controllers;

import urlshortener.utils.*;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;


@Controller
@EnableSwagger2
public class UrlShortenerController {

    @Autowired
    private StringRedisTemplate constantsMap;

    @Autowired
    private StringRedisTemplate urlsMap;

    /*
    @GetMapping("/r/{id:(?!link|index).*}")
    public ResponseEntity<Void> redirectTo(@PathVariable String id) {
        String key = urlsMap.opsForValue().get(id);
        if (key != null) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(URI.create(key));
            return new ResponseEntity<>(responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/link")
    public ResponseEntity<String> shortener(@RequestParam("url") String url, HttpServletRequest req) {
        if (UrlUtils.theURLisValid(url)) {
            String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
            urlsMap.opsForValue().set(id, url);
            constantsMap.opsForValue().increment("URLs");
            URI location = URI.create(req.getRequestURL().append("/").append(id).toString());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(location);
            return new ResponseEntity<>(id, responseHeaders, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
     }
     */
}