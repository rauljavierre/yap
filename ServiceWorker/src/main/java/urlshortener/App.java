package urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    static final String topicExchangeName = "";

    static final String queueName = "yap.request";



    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(App.class, args);
    }

}