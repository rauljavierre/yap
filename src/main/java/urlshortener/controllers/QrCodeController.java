package urlshortener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import urlshortener.services.QRService;
import org.springframework.stereotype.Controller;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.net.URI;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import java.util.Base64;
import com.google.zxing.WriterException;
import urlshortener.services.URLService;

import javax.servlet.http.HttpServletRequest;

@Controller
@EnableSwagger2
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
    public ResponseEntity<byte[]> qr(@PathVariable String hash,
                                     HttpServletRequest req) throws IOException, WriterException {

        if (!urlService.urlExists(hash)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        URI url = URI.create(req.getScheme() + "://" + req.getServerName() + "/" + hash);

        byte[] qrBase64;
        if(qrService.qrExists(hash)) {
            qrBase64 = qrService.getQR(hash);
            System.out.println("QR was generated before");
        }
        else {
            qrBase64 = qrService.generateAndStoreQR(url.toString(), hash);
            System.out.println("QR was not generated before");
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(url);
        responseHeaders.setContentType(MediaType.IMAGE_JPEG);

        CacheControl cacheControl = CacheControl.maxAge(60, TimeUnit.SECONDS).noTransform().mustRevalidate();
        responseHeaders.setCacheControl(cacheControl.toString());
        return new ResponseEntity<>(qrBase64, responseHeaders, HttpStatus.OK);
    }
}
