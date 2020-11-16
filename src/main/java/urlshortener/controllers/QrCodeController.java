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

import org.apache.commons.codec.binary.Base64;
import com.google.zxing.WriterException;
import javax.servlet.http.HttpServletRequest;

@Controller
@EnableSwagger2
public class QrCodeController {

    @Autowired
    private StringRedisTemplate urlsMap;

    @Autowired
    private StringRedisTemplate qrsMap;

    public QrCodeController(QRService qrService) {
        this.qrService = qrService;
    }

    @Autowired
    private final QRService qrService;

    @GetMapping("/qr/{hash}")
    public ResponseEntity<byte[]> qr(@PathVariable String hash,
                                     HttpServletRequest req) throws IOException, WriterException {

        System.out.println("/qr/" + hash);

        if (urlsMap.opsForValue().get(hash) == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        URI url = URI.create(req.getScheme() + "://" + req.getServerName() + "/" + hash);

        byte[] qrBase64 = Base64.decodeBase64(qrsMap.opsForValue().get(hash).getBytes());
        if(qrBase64.length != 0) {
            qrBase64 = qrService.qrGeneratorLibrary(url.toString());
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(url);
        responseHeaders.setContentType(MediaType.IMAGE_JPEG);

        CacheControl cacheControl = CacheControl.maxAge(60, TimeUnit.SECONDS).noTransform().mustRevalidate();
        responseHeaders.setCacheControl(cacheControl.toString());
        return new ResponseEntity<>(qrBase64, responseHeaders, HttpStatus.OK);
    }
}
