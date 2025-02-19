package com.salawubabatunde.seafarerbiometric.services;


import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Fid.Fiv;
import javafx.scene.shape.Ellipse;

import java.nio.ByteBuffer;

public class FingerprintImagePane extends Pane {
    private final Canvas canvas;
    private WritableImage writableImage;

    public FingerprintImagePane() {

        setPrefSize(128, 193);

        // Initialize the canvas
        canvas = new Canvas(128, 193);
        getChildren().add(canvas);

    }

    public void showImage(Fid image) {
        // Extract the fingerprint view
        Fiv view = image.getViews()[0];


        System.out.println(view);

        // Get the width, height, and image data
        int width = view.getWidth();
        int height = view.getHeight();
        byte[] grayscaleData = view.getImageData();


        // Convert grayscale data to BGRA format
        byte[] bgraData = convertGrayscaleToBGRA(grayscaleData, width, height);

        // Create a WritableImage for JavaFX
        writableImage = new WritableImage(width, height);

        // Use PixelFormat.getByteBgraInstance()
        PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraInstance();
        writableImage.getPixelWriter().setPixels(0, 0, width, height, pixelFormat, bgraData, 0, width * 4);

        // Draw the image on the canvas
        drawImage(width, height);
    }

    public void clearImage() {
        writableImage = null; // Remove the stored image
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear the canvas
        System.out.println("Fingerprint image cleared.");
    }


    private byte[] convertGrayscaleToBGRA(byte[] grayscaleData, int width, int height) {
        byte[] bgraData = new byte[width * height * 4];
        for (int i = 0; i < grayscaleData.length; i++) {
            int gray = grayscaleData[i] & 0xFF; // Convert to unsigned
            int bgraIndex = i * 4;
            bgraData[bgraIndex] = (byte) gray;     // Blue
            bgraData[bgraIndex + 1] = (byte) gray; // Green
            bgraData[bgraIndex + 2] = (byte) gray; // Red
            bgraData[bgraIndex + 3] = (byte) 255;  // Alpha
        }
        return bgraData;
    }

    private void drawImage(int imageWidth, int imageHeight) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (writableImage != null) {
            double x = (canvas.getWidth() - imageWidth) / 2;
            double y = (canvas.getHeight() - imageHeight) / 2;

            // Clip manually using an oval
            gc.save();
            gc.beginPath();
            gc.arc(canvas.getWidth() / 2, canvas.getHeight() / 2, 64, 96, 0, 360);
            gc.closePath();
            gc.clip();

            // Draw image
            gc.drawImage(writableImage, x, y);

            // Restore graphics context
            gc.restore();
        }
    }

    @Override
    protected void layoutChildren() {
        // Resize the canvas to match the size of the Pane
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());

        if (writableImage != null) {
            drawImage((int) writableImage.getWidth(), (int) writableImage.getHeight());
        }
    }
}
