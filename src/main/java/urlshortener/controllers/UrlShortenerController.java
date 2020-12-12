package urlshortener.controllers;

import com.google.zxing.WriterException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import urlshortener.domain.LinkBody;
import urlshortener.services.QRService;
import urlshortener.services.URLService;

@Controller
@EnableSwagger2
@CrossOrigin
public class UrlShortenerController {

    @Autowired
    private StringRedisTemplate map;

    @Autowired
    private final URLService urlService;

    @Autowired
    private final QRService qrService;

    public UrlShortenerController(URLService urlService, QRService qrService) {
        this.urlService = urlService;
        this.qrService = qrService;
    }

    @GetMapping("{hash}")
    public ResponseEntity<JSONObject> redirectTo(@PathVariable String hash) {
        System.out.println("/hash");

        JSONObject responseBody = new JSONObject();
        if (!urlService.urlExists(hash)){
            responseBody.put("error", "URL was not requested with /link");
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        }
        if(!urlService.urlStatusIsOk(hash)){
            responseBody.put("error", urlService.getUrl(hash));
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        }

        String url = urlService.getUrl(hash);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create(url));

        CacheControl cacheControl = CacheControl.maxAge(60*60*24*365, TimeUnit.SECONDS).noTransform().mustRevalidate();
        responseHeaders.setCacheControl(cacheControl.toString());
        return new ResponseEntity<>(responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
    }

    @RequestMapping(value = "/link", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> shortener(@RequestBody LinkBody linkBody, HttpServletRequest req) throws IOException, WriterException {
        System.out.println("/link");

        String url = linkBody.getUrl();
        boolean generateQR = linkBody.getGenerateQR();
        JSONObject responseBody = new JSONObject();
        if (url.equals("")) {
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }

        String hash = urlService.generateHashFromURL(url);
        String urlLocation = req.getScheme() + "://" + req.getServerName() + "/" + hash;

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create(urlLocation));

        responseBody.put("url", urlLocation);
        if(!qrService.qrExists(hash)) { // We
            qrService.generateAndStoreQR(urlLocation, hash);
        }
        if(generateQR) {
            String qrLocation = req.getScheme() + "://" + req.getServerName() + "/qr/" + hash;
            responseBody.put("qr", qrLocation);
        }

        if(urlService.urlExists(hash)) {
            if (urlService.getUrl(hash).equals(url)){
                return new ResponseEntity<>(responseBody, responseHeaders, HttpStatus.CREATED);
            }
        }

        Future<String> urlStatus = urlService.isValid(url);
        urlService.insertURLIntoREDIS(hash, url, urlStatus);

        return new ResponseEntity<>(responseBody, responseHeaders, HttpStatus.CREATED);
    }
}