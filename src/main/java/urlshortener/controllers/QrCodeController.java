package urlshortener.controllers;

import urlshortener.utils.*;

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

import org.springframework.web.bind.annotation.RestController;

@Controller
@EnableSwagger2
public class QrCodeController {

    private Map<String, String> lastAvailableMap = new HashMap<>();
    private Map<String, Channel> channelMap = null;
    private List<String> informationKeys = Arrays.asList(
            "total-memory-queue", "used-memory-queue",
            "available-memory-queue", "platform-queue", "cpu-used-queue",
            "cpu-cores-queue", "cpu-frequency-queue", "boot-time-queue");


    @Autowired
    private StringRedisTemplate sharedData;

    @GetMapping(value="/qr", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> qr(@RequestParam("url") String url) throws IOException,WriterException {
        boolean urlValid = UrlUtils.validateUrl(url);
        if (urlValid) {
            URI initialURL = URI.create(url);
            byte[] response = QrCodeUtils.qrGeneratorLibrary(url);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(initialURL);
            responseHeaders.setContentType(MediaType.IMAGE_JPEG);
            responseHeaders.setContentLength(response.length);
            return new ResponseEntity<>(response,responseHeaders, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/qrTime")
    public ResponseEntity<String> qrTime(@RequestParam("url") String url) throws IOException, WriterException {
        boolean urlValid = UrlUtils.validateUrl(url);
        if (urlValid) {
            long t = System.currentTimeMillis();
            byte[] response = QrCodeUtils.qrGeneratorLibrary(url);
            t = System.currentTimeMillis() - t;
            String r = Long.toString(t);
            return new ResponseEntity<>(r, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/qrAPI", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> qrAPI(@RequestParam("url") String url) throws IOException{
        boolean urlValid = UrlUtils.validateUrl(url);
        if (urlValid) {
            byte[] response = QrCodeUtils.qrGeneratorAPI(url);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.IMAGE_JPEG);
            responseHeaders.setContentLength(response.length);
            return new ResponseEntity<>(response,responseHeaders, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/qrAPITime")
    public ResponseEntity<String> qrAPITime(@RequestParam("url") String url) throws IOException{
        boolean urlValid = UrlUtils.validateUrl(url);
        if (urlValid) {
            long t = System.currentTimeMillis();
            byte[] response = QrCodeUtils.qrGeneratorAPI(url);
            t = System.currentTimeMillis() - t;
            String r = Long.toString(t);
            return new ResponseEntity<>(r, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}