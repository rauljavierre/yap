package urlshortener;

import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


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

    @GetMapping("/{id:(?!link|index).*}")
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
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        if (url != null && urlValidator.isValid(url) && this.urlExists(url)) {
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
        initialize();   // initialize connections with rabbitmq if needed

        info.put("NumberOfURLsStored", getNumberOfURLs());
        info.put("TotalMemory", readFromRabbitMQ("total-memory-queue"));
        info.put("UsedMemory", readFromRabbitMQ("used-memory-queue"));
        info.put("AvailableMemory", readFromRabbitMQ("available-memory-queue"));
        info.put("Platform", readFromRabbitMQ("platform-queue"));
        info.put("UsageOfCPU", readFromRabbitMQ("cpu-used-queue"));
        info.put("NumberOfCores", readFromRabbitMQ("cpu-cores-queue"));
        info.put("CPUFrequency", readFromRabbitMQ("cpu-frequency-queue"));
        info.put("BootTime", readFromRabbitMQ("boot-time-queue"));

        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    /*
        API: https://qrickit.com/qrickit_apps/qrickit_api.php
     */
    @GetMapping(value="/qr", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> shortener(@RequestParam("url") String url) throws IOException {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        if (url != null && urlValidator.isValid(url) && this.urlExists(url)) {
            String api = "https://qrickit.com/api/qr.php";
            String myQRRequest = api + "?d=" + url + "&t=j&qrsize=400";
            URI initialURL = URI.create(url);
            URL apiURL = new URL(myQRRequest);

            BufferedImage image = ImageIO.read(apiURL);
            ByteArrayOutputStream aux = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", aux);
            byte[] response = aux.toByteArray();

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(initialURL);
            responseHeaders.setContentType(MediaType.IMAGE_JPEG);
            responseHeaders.setContentLength(response.length);

            return new ResponseEntity<>(response,responseHeaders, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value="/csv-file", produces="text/csv")
    public ResponseEntity<String> shortener(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            // Parse file and get URL list
            List<String> longURLs = getCSVUrls(file);
            List<String> shorURLs = new ArrayList<>();
            // Iterate the URL list
            UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
            for (String currentURL : longURLs) {
                // Validate current URL
                if (currentURL != null && urlValidator.isValid(currentURL) && this.urlExists(currentURL)) {
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

    private String readFromRabbitMQ(String key) {
        try {
            String info = new String(this.channelMap.get(key).basicGet(key, false).getBody(), StandardCharsets.UTF_8);
            this.lastAvailableMap.put(key, info);
            return info;
        }
        catch (NullPointerException | IOException e){   // No updates | Connection Problems
            return lastAvailableMap.get(key);
        }
    }

    private String getNumberOfURLs() {
        Set<byte[]> keys = sharedData.getConnectionFactory().getConnection().keys("*".getBytes());
        return Long.toString(keys.size());
    }

    // https://www.rgagnon.com/javadetails/java-0059.html
    public static boolean urlExists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {   // Timeouts...
            return false;
        }
    }

    private void initialize() {
        if(channelMap == null) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq");
            try {
                channelMap = new HashMap<>();
                Connection connection = factory.newConnection();
                Map<String, Object> args = new HashMap<>();
                args.put("x-max-length", 1);
                for (String key : informationKeys) {
                    Channel c = connection.createChannel();
                    this.channelMap.put(key, c);
                    c.queueDeclare(key, false, false, false, args);
                }
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    // Read the CSV and return a list of URLs.
    private List<String> getCSVUrls (MultipartFile f) {
        String fileContent = "";
        try {
            fileContent = new String(f.getBytes(), StandardCharsets.UTF_8);
            fileContent = fileContent.replace("\n", "").replace("\r", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(Arrays.asList(fileContent.split(",")));
    }

}