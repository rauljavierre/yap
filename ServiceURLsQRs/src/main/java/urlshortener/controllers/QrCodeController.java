package urlshortener.controllers;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import urlshortener.services.QRService;
import org.springframework.stereotype.Controller;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.google.zxing.WriterException;
import urlshortener.services.URLService;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
//@EnableSwagger2
@CrossOrigin
public class QrCodeController {

    private String SCHEME_HOST = "http://yapsh.tk/";

    public QrCodeController(QRService qrService, URLService urlService) {
        this.qrService = qrService;
        this.urlService = urlService;
    }

    @Autowired
    private final QRService qrService;

    @Autowired
    private final URLService urlService;

    @GetMapping(value = "/qr/{hash}")
    public ResponseEntity<?> qr(@PathVariable String hash) throws IOException, WriterException {
        System.out.println("/qr/" + hash);


        if (!urlService.urlExists(hash)){
            JSONObject responseBody = new JSONObject();
            responseBody.put("error", "URL was not requested with /link");
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        }
        if(!urlService.urlStatusIsOk(hash)){
            JSONObject responseBody = new JSONObject();
            responseBody.put("error", urlService.getUrl(hash));
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        }
        //Link link = linkTo(UrlShortenerController.class).slash(hash).withSelfRel();
        String urlLocation = SCHEME_HOST + hash;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create(urlLocation));

        byte[] qrBase64;
        if(qrService.qrExists(hash)) {
            qrBase64 = qrService.getQR(hash);

            // Cache the response only if the QR is not null
            CacheControl cacheControl = CacheControl.maxAge(60*60*24*365, TimeUnit.SECONDS).noTransform().mustRevalidate();
            responseHeaders.setCacheControl(cacheControl.toString());
        }
        else {
            // qrBase64 may be null: polling with intervals in frontend
            qrBase64 = qrService.generateAndStoreQR(urlLocation, hash);
        }

        responseHeaders.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(qrBase64, responseHeaders, HttpStatus.OK);
    }
}
