package urlshortener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import urlshortener.utils.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.net.URI;
import java.io.IOException;
import com.google.zxing.WriterException;



@Controller
@EnableSwagger2
public class QrCodeController {

    @Autowired
    private StringRedisTemplate constantsMap;

    @PostMapping(value="/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<String> qr(@RequestParam("url") String url) throws IOException,WriterException {
        // Para comprobar el local que el QR se genera con la URL acortada
        String localhost = url.substring(0,9);
        boolean isLocalHost = localhost.equals("localhost");
        if (url.equals("")) {
            URI initialURL = URI.create(url);
            String response = QrCodeUtils.qrGeneratorLibrary(url);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(initialURL);
            responseHeaders.setContentType(MediaType.IMAGE_JPEG);
            constantsMap.opsForValue().increment("QRs");
            return new ResponseEntity<>(response,responseHeaders, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /*
    @GetMapping(value="/qrTime")
    public ResponseEntity<String> qrTime(@RequestParam("url") String url) throws IOException, WriterException {
        boolean urlValid = UrlUtils.theURLisValid(url);
        if (urlValid) {
            long t = System.currentTimeMillis();
            QrCodeUtils.qrGeneratorLibrary(url);
            t = System.currentTimeMillis() - t;
            String r = Long.toString(t);
            constantsMap.opsForValue().increment("QRs");
            return new ResponseEntity<>(r, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/qrAPI", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> qrAPI(@RequestParam("url") String url) throws IOException{
        boolean urlValid = UrlUtils.theURLisValid(url);
        if (urlValid) {
            byte[] response = QrCodeUtils.qrGeneratorAPI(url);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.IMAGE_JPEG);
            responseHeaders.setContentLength(response.length);
            constantsMap.opsForValue().increment("QRs");
            return new ResponseEntity<>(response,responseHeaders, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/qrAPITime")
    public ResponseEntity<String> qrAPITime(@RequestParam("url") String url) throws IOException{
        boolean urlValid = UrlUtils.theURLisValid(url);
        if (urlValid) {
            long t = System.currentTimeMillis();
            QrCodeUtils.qrGeneratorAPI(url);
            t = System.currentTimeMillis() - t;
            String r = Long.toString(t);
            constantsMap.opsForValue().increment("QRs");
            return new ResponseEntity<>(r, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
     */
}
