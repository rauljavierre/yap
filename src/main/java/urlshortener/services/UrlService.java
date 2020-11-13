package urlshortener.services;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class UrlService {

    public String isValid(String URL) {
        if(URL == null) {
            return "URL is null";
        }
        if(!(URL.startsWith("http") || URL.startsWith("https"))){
            return "URL is malformed";
        }

        HttpComponentsClientHttpRequestFactory httpClient = new HttpComponentsClientHttpRequestFactory();
        try {
            ClientHttpRequest request = httpClient.createRequest(new URI(URL), HttpMethod.HEAD);
            request.execute();
            return "URL is OK";
        }
        catch (URISyntaxException | IOException e){
            return "URL not reachable";
        }
    }
}