package urlshortener.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private URLService urlService;

    public String generateCSVLine(String url) {

        String hash = urlService.generateHashFromURL(url);
        if(urlService.urlExists(hash)) {
            if (urlService.getUrl(hash).equals(url)){
                return url + "," + SCHEME_HOST + hash + ",";
            }
        }

        Future<String> urlStatus = urlService.isValid(url);
        String response = url + ",";

        String urlStatusResult = "";
        try {
            urlStatusResult = urlStatus.get(15000, MILLISECONDS);

        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            logger.log(Level.WARNING, e.getClass().getName());  // TimeoutException with stress tests...
            return response + "," + "URL not reachable";
        }

        if (urlStatusResult.equals("URL is OK")) {
            urlService.insertURLIntoREDIS(hash,url,urlStatus);
            String newShortURL = SCHEME_HOST + hash;
            response = response + newShortURL + "," ;
        }
        else {
            response = response + "," + urlStatusResult;
        }

        logger.log(Level.WARNING, "response: " + response);
        return response;
    }
}