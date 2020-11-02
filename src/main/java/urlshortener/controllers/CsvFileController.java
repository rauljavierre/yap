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

//FOR QR


@Controller
@EnableSwagger2
public class CsvFileController {

    @Autowired
    private StringRedisTemplate urlsMap;

    @Autowired
    private StringRedisTemplate constantsMap;

    @PostMapping(value="/csv-file", produces="text/csv")
    public ResponseEntity<String> shortener(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            // Parse file and get URL list
            List<String> longURLs = CsvUtils.getCSVUrls(file);
            List<String> shorURLs = new ArrayList<>();
            // Iterate the URL list
            UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
            for (String currentURL : longURLs) {
                // Validate current URL
                if (currentURL != null && urlValidator.isValid(currentURL) && UrlUtils.urlExists(currentURL)) {
                    // Generate the short URL
                    String id = Hashing.murmur3_32().hashString(currentURL, StandardCharsets.UTF_8).toString();
                    urlsMap.opsForValue().set(id, currentURL);
                    shorURLs.add(id);
                }
                else {
                    shorURLs.add("invalidURL");
                }
            }
            // Generate CSV file with the short URL list
            try {
                /*
                TODO: Make this work
                File f = new File("shortener.csv");
                f.setReadable(true);
                f.setWritable(true);
                FileWriter csvWriter = new FileWriter(f);
                for (int i = 0; i < shorURLs.size() - 1; ++i) {
                    String newShortURL = shorURLs.get(i);
                    csvWriter.append(newShortURL);
                    csvWriter.append(",");
                }
                csvWriter.append(shorURLs.get(shorURLs.size()-1));
                csvWriter.flush();
                csvWriter.close();
                 */
                StringBuilder f = new StringBuilder();
                for (int i = 0; i < shorURLs.size(); ++i) {
                    String newShortURL = shorURLs.get(i);
                    f.append(newShortURL);
                    if (i != shorURLs.size() - 1) {
                        f.append(",");
                    }
                }
                // Generate ResponseEntity
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.parseMediaType("text/csv"));
                responseHeaders.setContentLength(f.length());
                constantsMap.opsForValue().increment("CSVs");
                return new ResponseEntity<>(f.toString(),responseHeaders,HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}