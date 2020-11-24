package urlshortener.services;

import com.google.common.hash.Hashing;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import urlshortener.endpoints.CSVEndpoint;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
public class CSVService {

    private String SCHEME_HOST = "http://yapsh.tk/";
    Logger logger = Logger.getLogger(CSVService.class.getName());

    @Autowired
    private final URLService urlService;

    public CSVService(URLService urlService) {
        this.urlService = urlService;
    }

    @Async
    public String generateCSVLine(String url) {
        Future<String> urlStatus = urlService.isValid(url);
        String response = "";
        try {
            String urlStatusResult = urlStatus.get(1500, MILLISECONDS);
            logger.log(Level.WARNING, "urlStatus: " + urlStatusResult);

            if (urlStatusResult.equals("URL is OK")) {
                String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
                urlService.insertURLIntoREDIS(id,url,urlStatus);
                String newShortURL = SCHEME_HOST + id;
                response = response + newShortURL + "," ;
            }
            else {
                response = response + ",";
                response = response + urlStatusResult;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            response = response + ",";
            response = response + "URL not reachable";
        }

        logger.log(Level.WARNING, "response: " + response);
        return response;
    }

}
