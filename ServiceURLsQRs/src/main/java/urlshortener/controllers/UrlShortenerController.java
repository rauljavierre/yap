package urlshortener.controllers;

import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import urlshortener.domain.LinkBody;
import urlshortener.services.QRService;
import urlshortener.services.URLService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@EnableSwagger2
public class UrlShortenerController {

    private String SCHEME_HOST = "http://localhost/";

    @Autowired
    private final URLService urlService;

    @Autowired
    private final QRService qrService;

    public UrlShortenerController(URLService urlService, QRService qrService) {
        this.urlService = urlService;
        this.qrService = qrService;
    }

    @Operation(summary = "Redirects to an URL given a short URL")
    @ApiResponses(
    value = {
            @ApiResponse(
                    responseCode = "307",
                    description = "Redirect to the URL"),

            @ApiResponse(
                    responseCode = "404",
                    description = "The short URL is invalid",
                    content = @Content(mediaType = "application/json")),

            @ApiResponse(
                    responseCode = "406",
                    description = "The URL is not been validated yet",
                    content = @Content(mediaType = "application/json")),

            @ApiResponse(responseCode = "500", content = @Content)
    })
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
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_ACCEPTABLE);
        }

        String url = urlService.getUrl(hash);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create(url));

        CacheControl cacheControl = CacheControl.maxAge(60*60*24*365, TimeUnit.SECONDS).noTransform().mustRevalidate();
        responseHeaders.setCacheControl(cacheControl.toString());
        return new ResponseEntity<>(responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
    }

    @Operation(summary = "Generates a short URL and a QR (if specified) given an URL")
    @ApiResponses(
    value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Short URL returned. QR returned if specified",
                    content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "URL could not be processed",
                    content = @Content(mediaType = "application/json")),

            @ApiResponse(responseCode = "500", content = @Content)
    })
    @RequestMapping(value = "/link", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> shortener(@RequestBody LinkBody linkBody) throws IOException, WriterException {
        System.out.println("/link");

        String url = linkBody.getUrl();
        JSONObject responseBody = new JSONObject();
        if (url == null || url.equals("")) {
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }

        String hash = urlService.generateHashFromURL(url);
        String urlLocation = SCHEME_HOST + hash;

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create(urlLocation));

        responseBody.put("url", urlLocation);
        if(!qrService.qrExists(hash)) {
            qrService.generateAndStoreQR(urlLocation, hash);
        }
        if(linkBody.getGenerateQR()) {
            String qrLocation = SCHEME_HOST + "qr/" + hash;
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

    @RequestMapping(value = "/check", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> check(@RequestParam String url) throws ExecutionException, InterruptedException, UnsupportedEncodingException {
        System.out.println("/check");

        JSONObject responseBody = new JSONObject();
        HttpHeaders responseHeaders = new HttpHeaders();

        if (url.equals("")) {
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }

        url = URLDecoder.decode(url, "UTF-8");

        Future<String> isValid = urlService.isValid(url);
        responseBody.put("isValid", isValid.get());
        responseBody.put("url", url);

        CacheControl cacheControl = CacheControl.maxAge(60*30, TimeUnit.SECONDS).noTransform().mustRevalidate();
        responseHeaders.setCacheControl(cacheControl.toString());

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}