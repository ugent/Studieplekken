package blok2.controllers;

import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This controller handles all requests related to barcodes.
 * Such as generating barcodes, responding with images and the ability to
 * download these images.
 */
@RestController
@RequestMapping("barcode")
public class BarcodeController {

    // creates an UPC-A barcode image from a given number
    public static BufferedImage generateUPCABarcodeImage(String barcodeText) {
        UPCABean barcodeGenerator = new UPCABean();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(160, BufferedImage.TYPE_BYTE_BINARY, false, 0);

        barcodeGenerator.generateBarcode(canvas, barcodeText);
        return canvas.getBufferedImage();
    }

    // creates an EAN13 barcode image from a given number
    public static BufferedImage generateEAN13BarcodeImage(String barcodeText) {
        EAN13Bean barcodeGenerator = new EAN13Bean();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(160, BufferedImage.TYPE_BYTE_BINARY, false, 0);

        barcodeGenerator.generateBarcode(canvas, barcodeText);
        return canvas.getBufferedImage();
    }

    @GetMapping(value = "/upca/{content}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> barbecueUPCABarcode(@PathVariable("content") String content) {
        return okResponse(BarcodeController.generateUPCABarcodeImage(content));
    }

    @GetMapping(value = "/ean13/{content}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> barbecueEAN13Barcode(@PathVariable("content") String content) {
        return okResponse(BarcodeController.generateEAN13BarcodeImage(content));
    }

    @GetMapping(value = "/{content}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> barbecueBarcode(@PathVariable("content") String content) {
        if (content.length() == 12) {
            return barbecueUPCABarcode(content);
        } else {
            return barbecueEAN13Barcode(content);
        }
    }

    @GetMapping(value = "/download/upca/{content}")
    public ResponseEntity<Resource> downloadUPCABarcode(@PathVariable("content") String content) throws IOException {
        return constructImagePacket(generateUPCABarcodeImage(content));
    }

    @GetMapping(value = "/download/ean13/{content}")
    public ResponseEntity<Resource> downloadEAN13Barcode(@PathVariable("content") String content) throws IOException {
        return constructImagePacket(generateEAN13BarcodeImage(content));
    }

    @GetMapping(value = "/download/{content}")
    public ResponseEntity<Resource> downloadBarcode(@PathVariable("content") String content) throws IOException {
        if (content.length() == 12) {
            return downloadUPCABarcode(content);
        } else {
            return downloadEAN13Barcode(content);
        }
    }

    // packages an image into a HTTP response that the user can use to download an image
    public ResponseEntity<Resource> constructImagePacket(BufferedImage image) throws IOException {
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=img.jpg");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        header.add("Content-Type", "application - download");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", bos);
        byte[] data = bos.toByteArray();

        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(data.length)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    // packages a image into a HTTP response
    private ResponseEntity<BufferedImage> okResponse(BufferedImage image) {
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    // function to create a UPC-A barcode from a given number
    // precondition is that the number is 12 digits long
    public static String calculateUPCACheckSum(String number) {
        char[] arr = number.toCharArray();
        int oddNumberDigits = 0;
        int evenNumberDigits = 0;
        for (int i = 0; i < arr.length; i++) {
            if (i % 2 == 0) {
                evenNumberDigits += Integer.parseInt(arr[i] + "");
            } else {
                oddNumberDigits += Integer.parseInt(arr[i] + "");
            }
        }
        evenNumberDigits *= 3;
        int sum = evenNumberDigits + oddNumberDigits;
        sum %= 10;
        if (sum != 0) {
            sum = 10 - sum;
        }
        return number + sum;
    }
}
