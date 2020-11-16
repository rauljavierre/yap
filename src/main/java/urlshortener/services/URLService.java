package urlshortener.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class URLService {

    @Autowired
    private StringRedisTemplate constantsMap;

    @Autowired
    private StringRedisTemplate urlsMap;

    @Async
    public Future<String> isValid(String URL) {
        if(URL == null) {
            return new AsyncResult<>("URL is null");
        }
        if(!(URL.startsWith("http") || URL.startsWith("https"))){
            return new AsyncResult<>("URL is malformed");
        }

        HttpComponentsClientHttpRequestFactory httpClient = new HttpComponentsClientHttpRequestFactory();
        try {
            ClientHttpRequest request = httpClient.createRequest(new URI(URL), HttpMethod.HEAD);
            request.execute();
            return new AsyncResult<>("URL is OK");
        }
        catch (URISyntaxException | IOException e){
            return new AsyncResult<>("URL not reachable");
        }
    }

    @Async
    public void insertURLIntoREDIS(String hash, String url, Future<String> urlStatus) {
        try {
            String status = urlStatus.get(10, TimeUnit.SECONDS);
            if (status.equals("URL is OK")) {
                urlsMap.opsForValue().set(hash, url);
                constantsMap.opsForValue().increment("URLs");
            }
        }
        catch (TimeoutException | InterruptedException | ExecutionException e) {
            System.out.println("Not inserting " + url + " because it is not responding");
        }
    }
}