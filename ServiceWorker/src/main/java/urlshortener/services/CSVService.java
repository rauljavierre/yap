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

@Service
public class CSVService {

    private String SCHEME_HOST = "http://localhost/";

    @Autowired
    private URLService urlService;

    public String generateCSVLine(String url) throws IOException, ParseException {
        String hash = urlService.generateHashFromURL(url);
        if(urlService.urlExists(hash)) {
            if (urlService.getUrl(hash).equals(url)){
                return url + "," + SCHEME_HOST + hash + ",";
            }
        }

        String urlStatusResult = isValid(url);

        String response = url + ",";
        if (urlStatusResult.equals("URL is OK")) {
            urlService.insertURLIntoREDIS(hash,url);
            String newShortURL = SCHEME_HOST + hash;
            response = response + newShortURL + "," ;
        }
        else {
            response = response + "," + urlStatusResult;
        }

        return response;
    }

    // Using another microservice to check if the URL given is reachable or not
    private String isValid(String url) throws IOException, ParseException {
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
        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(urlStatusResult);

        // The result is given in the property "isValid" of the response
        return json.get("isValid").toString();
    }
}
