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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class URLService {

    @Autowired
    private StringRedisTemplate map;

    @Async
    public Future<String> isValid(String url) {
        if(url == null) {
            return new AsyncResult<>("URL is null");
        }
        if(!(url.startsWith("http") || url.startsWith("https"))){
            return new AsyncResult<>("URL is malformed");
        }

        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("HEAD");
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return new AsyncResult<>("URL is OK");
            }
            else {
                return new AsyncResult<>("URL not reachable");
            }
        }
        catch (IOException e){
            return new AsyncResult<>("URL not reachable");
        }
    }

    @Async
    public void insertURLIntoREDIS(String hash, String url, Future<String> urlStatus) {
        try {
            String status = urlStatus.get(10, TimeUnit.SECONDS);
            if (status.equals("URL is OK")) {
                map.opsForValue().set(hash, url);
                map.opsForValue().increment("URLs");
            }
            else {
                System.out.println("Not inserting " + url + " because " + status);
            }
        }
        catch (TimeoutException | InterruptedException | ExecutionException e) {
            System.out.println("Not inserting " + url + " because it is not responding");
        }
    }
}