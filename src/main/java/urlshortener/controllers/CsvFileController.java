package urlshortener.controllers;

import urlshortener.utils.*;
import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.nio.charset.StandardCharsets;
import java.util.*;
import urlshortener.services.UrlService;


@Controller
@EnableSwagger2
public class CsvFileController {

    @Autowired
    private StringRedisTemplate urlsMap;

    @Autowired
    private StringRedisTemplate constantsMap;

    private final UrlService urlService;

    public CsvFileController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping(value="/csv-file", produces="text/csv")
    public ResponseEntity<String> shortener(@RequestParam("file") MultipartFile file) {
        if(file.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<String> longURLs = CsvUtils.getCSVUrls(file);
        List<String> shortURLs = new ArrayList<>();

        for (String currentURL : longURLs) {
            String urlStatus = urlService.isValid(currentURL);

            if(urlStatus.equals("URL is OK")) {
                String id = Hashing.murmur3_32().hashString(currentURL, StandardCharsets.UTF_8).toString();
                urlsMap.opsForValue().set(id, currentURL);
                shortURLs.add(id);
            }
            else {
                shortURLs.add(urlStatus);
            }
        }

        StringBuilder f = new StringBuilder();
        for (int i = 0; i < shortURLs.size(); ++i) {
            String newShortURL = shortURLs.get(i);
            f.append(newShortURL);
            if (i != shortURLs.size() - 1) {
                f.append(",");
            }
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType("text/csv"));
        responseHeaders.setContentLength(f.length());
        constantsMap.opsForValue().increment("CSVs");

        return new ResponseEntity<>(f.toString(),responseHeaders,HttpStatus.OK);
    }
}