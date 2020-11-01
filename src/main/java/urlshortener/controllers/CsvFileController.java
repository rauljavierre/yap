package urlshortener.controllers;

import urlshortener.utils.*;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

//FOR QR
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;


@Controller
@EnableSwagger2
public class CsvFileController {

    private Map<String, String> lastAvailableMap = new HashMap<>();
    private Map<String, Channel> channelMap = null;
    private List<String> informationKeys = Arrays.asList(
            "total-memory-queue", "used-memory-queue",
            "available-memory-queue", "platform-queue", "cpu-used-queue",
            "cpu-cores-queue", "cpu-frequency-queue", "boot-time-queue");

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    @Autowired
    private StringRedisTemplate sharedData;


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
                    sharedData.opsForValue().set(id, currentURL);
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