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
    private StringRedisTemplate urlsMap;

    @Autowired
    private StringRedisTemplate constantsMap;

    private final URLService urlService;

    public CsvFileController(URLService urlService) {
        this.urlService = urlService;
    }

    @PostMapping(value="/csv-file", produces="text/csv")
    public ResponseEntity<String> shortener(@RequestParam("file") MultipartFile file, HttpServletRequest req) {
        // TODO: test with future<String>...

        if(file.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<String> longURLs = CsvUtils.getCSVUrls(file);
        List<String> shortURLs = new ArrayList<>();

        for (String currentURL : longURLs) {
            Future<String> urlStatus = urlService.isValid(currentURL);
            try {
                String urlStatusResult = urlStatus.get(1500, MILLISECONDS);
                if(urlStatusResult.equals("URL is OK")) {
                    String id = Hashing.murmur3_32().hashString(currentURL, StandardCharsets.UTF_8).toString();
                    shortURLs.add(id);
                }
                else {
                    shortURLs.add(urlStatusResult);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                shortURLs.add("URL not reachable");
            }
        }

        StringBuilder f = new StringBuilder();
        for (int i = 0; i < shortURLs.size(); ++i) {
            String newShortURL = req.getScheme() + "://" + req.getServerName() + "/" + shortURLs.get(i);
            f.append(",");
            f.append(longURLs.get(i));
            f.append(newShortURL);
            if (i != shortURLs.size() - 1) {
                f.append("\n");
            }
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Disposition", "attachment; filename=" + "shortener.csv");
        responseHeaders.setContentType(MediaType.parseMediaType("text/csv"));
        responseHeaders.setContentLength(f.length());
        String urlLocation = req.getScheme() + "://" + req.getServerName() + "/" + shortURLs.get(0);
        responseHeaders.setLocation(URI.create(urlLocation));

        constantsMap.opsForValue().increment("CSVs");
        return new ResponseEntity<>(f.toString(),responseHeaders,HttpStatus.CREATED);
    }
}