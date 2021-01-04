package urlshortener.services;

import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;


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
    public void insertURLIntoREDIS(String hash, String url) {
        map.opsForValue().set(hash, url);       // http://
        map.opsForValue().increment("URLs");
        System.out.println("Inserting " + url + " with hash " + hash);
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