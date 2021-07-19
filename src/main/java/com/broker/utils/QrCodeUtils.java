package com.broker.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.io.OutputStream;

public class QrCodeUtils {

    private static final String QR_CODE_IMAGE_PATH = "F:\\code\\sunsmc\\MyQRCode.png";
    private static final QRCodeWriter qrCodeWriter = new QRCodeWriter();
    private static final int width = 350;
    private static final int height = 350;

    public static void generateQRCodeImage(String text, OutputStream outputStream) throws WriterException, IOException {

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
    }

    public static void main(String[] args) {
        try {
            generateQRCodeImage("This is my first QR Code", null);
        } catch (WriterException e) {
            System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Could not generate QR Code, IOException :: " + e.getMessage());
        }

    }
}
