package urlshortener.services;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class CSVService {

    private String SCHEME_HOST = "http://localhost/";

    Logger logger = Logger.getLogger(CSVService.class.getName());

    @Autowired
    private URLService urlService;

    public String generateCSVLine(String url) throws IOException, InterruptedException, ParseException {
        String hash = urlService.generateHashFromURL(url);
        if(urlService.urlExists(hash)) {
            if (urlService.getUrl(hash).equals(url)){
                return url + "," + SCHEME_HOST + hash + ",";
            }
        }

        String response = url + ",";

        String urlEncoded = URLEncoder.encode(url, "UTF-8");
        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://yap_nginx/check?url=" + urlEncoded).openConnection();
        urlConnection.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String urlStatusResult = sb.toString();
        logger.info(urlStatusResult);
        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(urlStatusResult);
        urlStatusResult = json.get("isValid").toString();

        logger.info(urlStatusResult);

        if (urlStatusResult.equals("URL is OK")) {
            urlService.insertURLIntoREDIS(hash,url,urlStatusResult);
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
