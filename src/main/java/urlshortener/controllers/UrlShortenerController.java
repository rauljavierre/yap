package urlshortener.controllers;

import org.json.simple.JSONObject;
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
import urlshortener.services.UrlService;

@Controller
@EnableSwagger2
public class UrlShortenerController {

    @Autowired
    private StringRedisTemplate constantsMap;

    @Autowired
    private StringRedisTemplate urlsMap;

    private final UrlService urlService;

    public UrlShortenerController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("{hash}")
    public ResponseEntity<Void> redirectTo(@PathVariable String hash) {
        String url = urlsMap.opsForValue().get(hash);
        if (url == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create(url));
        return new ResponseEntity<>(responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
    }

    @PostMapping("/link")
    public ResponseEntity<JSONObject> shortener(@RequestParam("url") String url,
                                            @RequestParam("generateQR") boolean generateQR,
                                            HttpServletRequest req) {

        // TODO: always returning 201????? And if it was created before?
        String urlStatus = urlService.isValid(url);
        if (urlStatus.equals("URL is OK")) {
            String hash = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
            String urlLocation = req.getScheme() + "://" + req.getServerName() + "/" + hash;
            String qrLocation = req.getScheme() + "://" + req.getServerName() + "/qr/" + hash;

            JSONObject responseBody = new JSONObject();
            responseBody.put("url", urlLocation);
            if(generateQR) {
                responseBody.put("qr", qrLocation);
            }

            urlsMap.opsForValue().set(hash, url);
            constantsMap.opsForValue().increment("URLs");

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(URI.create(urlLocation));

            return new ResponseEntity<>(responseBody, responseHeaders, HttpStatus.CREATED);
        }
        else {
            JSONObject responseBody = new JSONObject();
            responseBody.put("error", urlStatus);
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
     }

}