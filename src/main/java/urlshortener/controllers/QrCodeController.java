package urlshortener.controllers;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import urlshortener.services.QRService;
import org.springframework.stereotype.Controller;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.net.URI;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.zxing.WriterException;
import urlshortener.services.URLService;

import javax.servlet.http.HttpServletRequest;

@Controller
@EnableSwagger2
@CrossOrigin
public class QrCodeController {

    public QrCodeController(QRService qrService, URLService urlService) {
        this.qrService = qrService;
        this.urlService = urlService;
    }

    @Autowired
    private final QRService qrService;

    @Autowired
    private final URLService urlService;

    @GetMapping("/qr/{hash}")
    public ResponseEntity<JSONObject> qr(@PathVariable String hash,
                                         HttpServletRequest req) throws IOException, WriterException {
        System.out.println("/qr/" + hash);

        JSONObject responseBody = new JSONObject();
        if (!urlService.urlExists(hash)){
            responseBody.put("error", "URL was not requested with /link");
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        }
        if(!urlService.urlStatusIsOk(hash)){
            responseBody.put("error", urlService.getUrl(hash));
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        }

        URI url = URI.create(req.getScheme() + "://" + req.getServerName() + "/" + hash);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(url);

        byte[] qrBase64;
        if(qrService.qrExists(hash)) {
            qrBase64 = qrService.getQR(hash);

            // Cache the response only if the QR is not null
            CacheControl cacheControl = CacheControl.maxAge(60*60*24*365, TimeUnit.SECONDS).noTransform().mustRevalidate();
            responseHeaders.setCacheControl(cacheControl.toString());
        }
        else {
            // qrBase64 may be null: polling with intervals in frontend
            qrBase64 = qrService.generateAndStoreQR(url.toString(), hash);
        }
        responseBody.put("qr", qrBase64);

        return new ResponseEntity<>(responseBody, responseHeaders, HttpStatus.OK);
    }
}
