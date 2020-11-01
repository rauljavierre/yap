package urlshortener.utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;


public class CsvUtils {

    // Read the CSV and return a list of URLs.
    public static List<String> getCSVUrls (MultipartFile f) {
        String fileContent = "";
        try {
            fileContent = new String(f.getBytes(), StandardCharsets.UTF_8);
            fileContent = fileContent.replace("\n", "").replace("\r", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(Arrays.asList(fileContent.split(",")));
    }

}