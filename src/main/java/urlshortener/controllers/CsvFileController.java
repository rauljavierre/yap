package urlshortener.controllers;

import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import urlshortener.services.URLService;
import urlshortener.utils.CsvUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


@Controller
@EnableSwagger2
public class CsvFileController {

    @Autowired
    private StringRedisTemplate map;

    private final URLService urlService;

    public CsvFileController(URLService urlService) {
        this.urlService = urlService;
    }

    @PostMapping(value="/csv-file", produces="text/csv")
    public ResponseEntity<String> shortener(@RequestParam("file") MultipartFile file, HttpServletRequest req) {
        if(file.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        boolean locationSet = false;
        HttpHeaders responseHeaders = new HttpHeaders();
        List<String> longURLs = CsvUtils.getCSVUrls(file);

        StringBuilder f = new StringBuilder();
        for (String currentURL : longURLs) {
            f.append(currentURL);
            f.append(",");
            Future<String> urlStatus = urlService.isValid(currentURL);
            try {
                String urlStatusResult = urlStatus.get(1500, MILLISECONDS);
                if (urlStatusResult.equals("URL is OK")) {
                    String id = Hashing.murmur3_32().hashString(currentURL, StandardCharsets.UTF_8).toString();
                    urlService.insertURLIntoREDIS(id,currentURL,urlStatus);
                    String newShortURL = req.getScheme() + "://" + req.getServerName() + "/" + id;
                    f.append(newShortURL);
                    f.append(",");
                    if (!locationSet) {
                        locationSet = true;
                        responseHeaders.setLocation(URI.create(newShortURL));
                    }
                }
                else {
                    f.append(",");
                    f.append(urlStatusResult);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                f.append(",");
                f.append("URL not reachable");
            }
            f.append("\n");
        }

        responseHeaders.set("Content-Disposition", "attachment; filename=" + "shortener.csv");
        responseHeaders.setContentType(MediaType.parseMediaType("text/csv"));
        responseHeaders.setContentLength(f.length());

        map.opsForValue().increment("CSVs");
        return new ResponseEntity<>(f.toString(),responseHeaders,HttpStatus.CREATED);
    }
}