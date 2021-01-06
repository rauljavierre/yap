package urlshortener.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import urlshortener.services.QRService;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import com.google.zxing.WriterException;
import urlshortener.services.URLService;

@RestController
@CrossOrigin
@EnableSwagger2
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

    @Operation(summary = "Returns a base64 string of a QR image given a short URL")
    @ApiResponses(
    value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns the QR image",
                    content =
                    @Content(
                            mediaType = "image/png",
                            schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "The short URL is invalid or not validated yet",
                    content = @Content(mediaType = "application/json")),

            @ApiResponse(responseCode = "500", content = @Content)
    })
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

        String urlLocation = SCHEME_HOST + hash;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create(urlLocation));

        byte[] qrBase64;
        if(qrService.qrExists(hash)) {
            qrBase64 = qrService.getQR(hash);
        }
        else {
            qrBase64 = qrService.generateAndStoreQR(urlLocation, hash);
        }

        CacheControl cacheControl = CacheControl.maxAge(60*60*24*365, TimeUnit.SECONDS).noTransform().mustRevalidate();
        responseHeaders.setCacheControl(cacheControl.toString());
        responseHeaders.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(qrBase64, responseHeaders, HttpStatus.OK);
    }
}
