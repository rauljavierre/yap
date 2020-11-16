package urlshortener.controllers;

import com.google.zxing.WriterException;
import org.json.simple.JSONObject;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.CacheControl;
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
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import urlshortener.services.QRService;
import urlshortener.services.URLService;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Controller
@EnableSwagger2
public class UrlShortenerController {

    @Autowired
    private StringRedisTemplate urlsMap;

    @Autowired
    private final URLService urlService;

    @Autowired
    private final QRService qrService;

    public UrlShortenerController(URLService urlService, QRService qrService) {
        this.urlService = urlService;
        this.qrService = qrService;
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
                                            HttpServletRequest req) throws IOException, WriterException {

        // TODO: always returning 201????? And if it was created before?
        Future<String> urlStatus = urlService.isValid(url);

        String hash = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
        String urlLocation = req.getScheme() + "://" + req.getServerName() + "/" + hash;
        String qrLocation = req.getScheme() + "://" + req.getServerName() + "/qr/" + hash;

        JSONObject responseBody = new JSONObject();
        responseBody.put("url", urlLocation);
        if(generateQR) {
            qrService.generateAndStoreQR(urlLocation, hash);
            responseBody.put("qr", qrLocation);
        }

        return buildResponseEntity(urlStatus, urlLocation, responseBody, hash);
    }

    private ResponseEntity<JSONObject> buildResponseEntity(Future<String> urlStatus, String urlLocation, JSONObject responseBody, String hash) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create(urlLocation));
        try {
            String urlStatusResult = urlStatus.get(1500, MILLISECONDS);

            if(urlStatusResult.equals("URL is OK")){
                urlService.insertURLIntoREDIS(hash, urlLocation, urlStatus);
                CacheControl cacheControl = CacheControl.maxAge(60, TimeUnit.SECONDS).noTransform().mustRevalidate();
                responseHeaders.setCacheControl(cacheControl.toString());
                return new ResponseEntity<>(responseBody, responseHeaders, HttpStatus.CREATED);
            }
            else {
                responseBody = new JSONObject();
                responseBody.put("error", urlStatusResult);
                return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
            }
        }
        catch (TimeoutException | InterruptedException | ExecutionException e) {
            urlService.insertURLIntoREDIS(hash, urlLocation, urlStatus);
            responseBody = new JSONObject();
            responseBody.put("error", "URL not reachable");
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
    }
}