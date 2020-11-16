package urlshortener.services;

import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class QRService {

    @Autowired
    private StringRedisTemplate qrsMap;

    // Generates QR from url with library:
    // https://www.javadoc.io/doc/com.google.zxing/core/3.3.0/com/google/zxing/multi/qrcode/package-summary.html
    public static byte[] qrGeneratorLibrary(String url) throws IOException, WriterException {
        QRCodeWriter qr = new QRCodeWriter();
        BitMatrix matrix = qr.encode(url, BarcodeFormat.QR_CODE,400,400);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream aux = new ByteArrayOutputStream();
        ImageIO.write(image, "png", aux);
        return aux.toByteArray();
    }

    @Async
    public byte[] generateAndStoreQR(String url, String hash) throws IOException, WriterException {
        byte[] qrBase64 = qrGeneratorLibrary(url);
        qrsMap.opsForValue().set(hash, qrBase64.toString());
        return qrBase64;
    }
}