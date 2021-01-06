package urlshortener;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootConfiguration
public class OurThreadPool {

    @Bean
    ThreadPoolExecutor executor () {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);
        return threadPoolExecutor;
    }
}
