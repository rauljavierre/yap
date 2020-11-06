package urlshortener.utils;

import org.apache.commons.validator.routines.UrlValidator;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlUtils {

    // https://www.baeldung.com/java-check-url-exists
    public static boolean urlExists(String URLName){
        try {
            URL url = new URL(URLName);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");

            return (huc.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {   // Timeouts...
            return false;
        }
    }

    public static boolean theURLisValid(String url){
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        return (url != null && urlValidator.isValid(url) && urlExists(url));
    }
}