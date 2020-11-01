package urlshortener.controllers;

import urlshortener.utils.*;

import org.springframework.web.bind.annotation.RestController;
import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

//FOR QR
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;


@Controller
@EnableSwagger2
public class GetInfoController {

    private Map<String, String> lastAvailableMap = new HashMap<>();
    private Map<String, Channel> channelMap = null;
    private List<String> informationKeys = Arrays.asList(
            "total-memory-queue", "used-memory-queue",
            "available-memory-queue", "platform-queue", "cpu-used-queue",
            "cpu-cores-queue", "cpu-frequency-queue", "boot-time-queue");



    @Autowired
    private StringRedisTemplate sharedData;


    @GetMapping("/get_info")
    public ResponseEntity<HashMap<String, String>> getInfo() {
        HashMap<String, String> info = new HashMap<>();
        RabbitMQUtils.initialize(this.channelMap,this.informationKeys);   // initialize connections with rabbitmq if needed

        info.put("NumberOfURLsStored", RabbitMQUtils.getNumberOfURLs(sharedData));
        info.put("TotalMemory",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "total-memory-queue"));
        info.put("UsedMemory",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "used-memory-queue"));
        info.put("AvailableMemory",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "available-memory-queue"));
        info.put("Platform",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "platform-queue"));
        info.put("UsageOfCPU",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "cpu-used-queue"));
        info.put("NumberOfCores",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "cpu-cores-queue"));
        info.put("CPUFrequency",  RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "cpu-frequency-queue"));
        info.put("BootTime", RabbitMQUtils.readFromRabbitMQ(this.channelMap,this.lastAvailableMap,
                "boot-time-queue"));
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

}