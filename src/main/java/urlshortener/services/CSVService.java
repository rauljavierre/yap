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

    private String SCHEME_HOST = "http://localhost/";
    Logger logger = Logger.getLogger(CSVService.class.getName());

    @Autowired
    private final URLService urlService;

    public CSVService(URLService urlService) {
        this.urlService = urlService;
    }

    public String generateCSVLine(String url) throws InterruptedException {
        if (url.equals("http://google.es")) {
            Thread.sleep(10000);
        }

        String hash = urlService.generateHashFromURL(url);
        if(urlService.urlExists(hash)) {
            if (urlService.getUrl(hash).equals(url)){
                return url + "," + SCHEME_HOST + hash + ",";
            }
        }
        
        Future<String> urlStatus = urlService.isValid(url);
        String response = url + ",";
        logger.log(Level.WARNING, "response: " + response);
        logger.log(Level.WARNING, "urlService: " + urlService);

        String urlStatusResult = "";
        try {
            urlStatusResult = urlStatus.get(1500, MILLISECONDS);
            logger.log(Level.WARNING, "urlStatus: " + urlStatusResult);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            response = response + ",";
            response = response + "URL not reachable";
            return response;
        }

        if (urlStatusResult.equals("URL is OK")) {
            urlService.insertURLIntoREDIS(hash,url,urlStatus);
            String newShortURL = SCHEME_HOST + hash;
            response = response + newShortURL + "," ;
        }
        else {
            response = response + ",";
            response = response + urlStatusResult;
        }

        logger.log(Level.WARNING, "response: " + response);
        return response;
    }

}
