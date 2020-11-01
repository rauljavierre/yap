package urlshortener;

import utils.*;

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

@SpringBootApplication
@Controller
@EnableSwagger2
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

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

    @GetMapping("/r/{id:(?!link|index).*}")
    public ResponseEntity<Void> redirectTo(@PathVariable String id) {
        String key = sharedData.opsForValue().get(id);
        if (key != null) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(URI.create(key));
            return new ResponseEntity<>(responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/link")
    public ResponseEntity<String> shortener(@RequestParam("url") String url, HttpServletRequest req) {
        boolean urlValid = UrlUtils.validateUrl(url);
        if (urlValid) {
            String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
            sharedData.opsForValue().set(id, url);
            URI location = URI.create(req.getRequestURL().append("/"+id).toString());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(location);
            return new ResponseEntity<>(id, responseHeaders, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get_info")
    public ResponseEntity<HashMap<String, String>> getInfo() {
        HashMap<String, String> info = new HashMap<>();
        RabbitMQUtils.initialize(this.channelMap,this.informationKeys);   // initialize connections with rabbitmq if needed

        info.put("NumberOfURLsStored", RabbitMQUtils.getNumberOfURLs(sharedData));
        info.put("TotalMemory",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "total-memory-queue"));
        info.put("UsedMemory",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "used-memory-queue"));
        info.put("AvailableMemory",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "available-memory-queue"));
        info.put("Platform",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "platform-queue"));
        info.put("UsageOfCPU",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "cpu-used-queue"));
        info.put("NumberOfCores",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "cpu-cores-queue"));
        info.put("CPUFrequency",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "cpu-frequency-queue"));
        info.put("BootTime", RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "boot-time-queue"));
        return new ResponseEntity<>(info, HttpStatus.OK);
    }


    @GetMapping(value="/qr", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> qr(@RequestParam("url") String url) throws IOException,WriterException {
        boolean urlValid = UrlUtils.validateUrl(url);
        if (urlValid) {
            URI initialURL = URI.create(url);
            byte[] response = QrCodeUtils.qrGeneratorLibrary(url);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(initialURL);
            responseHeaders.setContentType(MediaType.IMAGE_JPEG);
            responseHeaders.setContentLength(response.length);
            return new ResponseEntity<>(response,responseHeaders, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/qrTime")
    public ResponseEntity<String> qrTime(@RequestParam("url") String url) throws IOException, WriterException {
        boolean urlValid = UrlUtils.validateUrl(url);
        if (urlValid) {
            long t = System.currentTimeMillis();
            byte[] response = QrCodeUtils.qrGeneratorLibrary(url);
            t = System.currentTimeMillis() - t;
            String r = Long.toString(t);
            return new ResponseEntity<>(r, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/qrAPI", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> qrAPI(@RequestParam("url") String url) throws IOException{
        boolean urlValid = UrlUtils.validateUrl(url);
        if (urlValid) {
            byte[] response = QrCodeUtils.qrGeneratorAPI(url);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.IMAGE_JPEG);
            responseHeaders.setContentLength(response.length);
            return new ResponseEntity<>(response,responseHeaders, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/qrAPITime")
    public ResponseEntity<String> qrAPITime(@RequestParam("url") String url) throws IOException{
        boolean urlValid = UrlUtils.validateUrl(url);
        if (urlValid) {
            long t = System.currentTimeMillis();
            byte[] response = QrCodeUtils.qrGeneratorAPI(url);
            t = System.currentTimeMillis() - t;
            String r = Long.toString(t);
            return new ResponseEntity<>(r, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

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