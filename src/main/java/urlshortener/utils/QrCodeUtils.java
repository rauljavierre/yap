package urlshortener.utils;

import java.io.*;
import java.util.Base64;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;


public class QrCodeUtils {

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

    // Generates QR from url with API:
    //  https://qrickit.com/qrickit_apps/qrickit_api.php
    public static byte[] qrGeneratorAPI(String url) throws IOException{
        String api = "https://qrickit.com/api/qr.php";
        String myQRRequest = api + "?d=" + url + "&t=j&qrsize=400";
        URL apiURL = new URL(myQRRequest);
        BufferedImage image = ImageIO.read(apiURL);
        ByteArrayOutputStream aux = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", aux);
        return aux.toByteArray();
    }

}