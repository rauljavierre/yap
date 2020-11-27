package urlshortener;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import urlshortener.endpoints.CSVEndpoint;
import urlshortener.services.CSVService;
import urlshortener.services.URLService;

@SpringBootConfiguration
@EnableWebSocket
public class SpringConfiguration {

    @Bean
    public ServerEndpointExporter serverEndpoint() {
        return new ServerEndpointExporter();
    }

    @Bean
    public CSVService generateCSVService() {
        return new CSVService();
    }
}
