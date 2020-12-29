package urlshortener.services;

import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class URLService {

    /*
    <"124891724", "http://airezico.tk">
    <"qr124891724", base64>
    <URLs, "3">
    <QRs, "7">
    <CSVs, "1">
     */
    @Autowired
    private StringRedisTemplate map;

    @Async
    public Future<String> isValid(String url) {
        if(url == null) {
            return new AsyncResult<>("URL is null");
        }
        if(!(url.startsWith("http://") || url.startsWith("https://"))){
            return new AsyncResult<>("URL is malformed");
        }

        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return new AsyncResult<>("URL is OK");
            }
            else if (urlConnection.getResponseCode() >= 300 && urlConnection.getResponseCode() <= 399){
                HttpURLConnection urlConnection2 = (HttpURLConnection) new URL(urlConnection.getHeaderField("Location")).openConnection();
                urlConnection2.setRequestMethod("GET");
                if (urlConnection2.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return new AsyncResult<>("URL is OK");
                }
                else {
                    return new AsyncResult<>("URL not reachable");
                }
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
            map.opsForValue().set(hash, "URL not validated yet");
            String status = urlStatus.get(10, TimeUnit.SECONDS);
            if (status.equals("URL is OK")) {
                map.opsForValue().set(hash, url);       // http://
                map.opsForValue().increment("URLs");
                System.out.println("Inserting " + url + " with hash " + hash);
            }
            else {
                map.opsForValue().set(hash, status);    // Error message
                System.out.println("Not inserting " + url + " because " + status);
            }
        }
        catch (TimeoutException | InterruptedException | ExecutionException e) {
            map.opsForValue().set(hash, "URL not reachable");    // Error message
            System.out.println("Not inserting " + url + " because it is not responding");
        }
    }

    public String generateHashFromURL(String url) {
        return Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
    }

    public boolean urlExists(String hash) {
        return map.opsForValue().get(hash) != null;
    }

    public String getUrl(String hash) {
        return map.opsForValue().get(hash);
    }
}