package utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class UrlUtils {

    // https://www.rgagnon.com/javadetails/java-0059.html
    public static boolean urlExists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {   // Timeouts...
            return false;
        }
    }

}