package com.salawubabatunde.seafarerbiometric.controllers;

import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Reader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salawubabatunde.seafarerbiometric.model.SeafarerData;
import com.salawubabatunde.seafarerbiometric.services.ApiService;
import com.salawubabatunde.seafarerbiometric.services.CaptureTask;
import com.salawubabatunde.seafarerbiometric.services.FingerprintImagePane;
import com.salawubabatunde.seafarerbiometric.services.ThumbFingerPrintImagePane;
import com.topaz.sigplus.SigPlus;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Ellipse;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.Beans;
import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureController implements Initializable {

    @FXML
    private Label instructionLbl;

    @FXML
    private Label loadingTxt;

    @FXML Pane sub_root;

    @FXML
    private Button signatureBtn;

    @FXML
    HBox notificationCenter;

    @FXML
    private Pane fingerPrintPreview, rightThumb, leftThumb, emailPane, infoPane, cameraPane, signaturePane;

    @FXML
    MFXTextField emailInput, firstName, lastName,seafarerID,nin;

    @FXML
    ImageView profileImg,cameraPreview, signatureImage;

    @FXML
    VBox loader, signatureContainer;

    @FXML
    HBox biometricBtnContainer, thumbImageContainer;

    private Reader reader;
    private FingerprintImagePane fingerprintImagePane;
    private ThumbFingerPrintImagePane thumbFingerPrintImagePaneRight, thumbFingerPrintImagePaneLeft;
    private CaptureTask captureTask;
    private Engine engine;
    private int rightCapture = 0;
    private int leftCapture = 0;
    private String seafarerEmail, seafarerNo;
    private VideoCapture capture;
    private boolean capturing = false;
    private Thread captureThread;
    private Mat lastFrame;

    private Fmd leftCaptureFMD, rightCaptureFMD;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SigPlus sigObj;

    private String capturedPictureBase64, capturedSignatureBase64;
    private ImageView signatureImageView;
    private HomeController homeController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        capture = new VideoCapture();

        homeController = new HomeController();
        // Initialize fingerprint components
        fingerprintImagePane = new FingerprintImagePane();
        thumbFingerPrintImagePaneLeft = new ThumbFingerPrintImagePane();
        thumbFingerPrintImagePaneRight = new ThumbFingerPrintImagePane();

        // Add fingerprint panes to UI containers
        fingerPrintPreview.getChildren().add(fingerprintImagePane);
        rightThumb.getChildren().add(thumbFingerPrintImagePaneRight);
        leftThumb.getChildren().add(thumbFingerPrintImagePaneLeft);

        // Apply oval clipping
        applyClipping(thumbFingerPrintImagePaneRight);
        applyClipping(thumbFingerPrintImagePaneLeft);

        //cameraPreview.setFitWidth(400);
        cameraPreview.setPreserveRatio(true);

        profileImg.setFitWidth(200);
        profileImg.setPreserveRatio(true);

       cleanUp();
       initializeReader();


    }

    private void applyClipping(Pane pane) {
        if (pane.getPrefWidth() > 0 && pane.getPrefHeight() > 0) {
            double radiusX = pane.getPrefWidth() / 2;
            double radiusY = pane.getPrefHeight() / 2;
            Ellipse clip = new Ellipse(radiusX, radiusY, radiusX, radiusY);
            pane.setClip(clip);
        } else {
            System.err.println("Warning: Invalid pane dimensions for clipping.");
        }
    }


    public void initializeReader() {

        try {
            UareUGlobal.DestroyReaderCollection();
        } catch (UareUException e) {
            throw new RuntimeException(e);
        }

        try {

           ReaderCollection readers = UareUGlobal.GetReaderCollection();
            readers.GetReaders();

            if (readers.isEmpty()) {
                System.out.println("No fingerprint readers found.");
                showNotification("No fingerprint readers found.","notificationLabel");
                return;
            }


           int readerNumber = readers.size() - 1;

            readers.forEach(reader1 -> {
                System.out.println("readers ======> " + reader1.GetDescription().id);
            });

            reader = readers.getLast();

            reader.Open(Reader.Priority.EXCLUSIVE);


            engine = UareUGlobal.GetEngine();


            showNotification("Using fingerprint reader: " + reader.GetDescription().name,"notificationLabelSuccess");
//            System.out.println("Using fingerprint reader: " + reader.GetDescription().technology.name());
            System.out.println("readers ========> "+ readers.size());
            System.out.println("current reader Number ========> "+ readerNumber);
            System.out.println("reader id ========> "+ readers.get(0).GetDescription().id);
           // System.out.println("reader id2 ========> "+ readers.getFirst().GetDescription().id);
//            System.out.println("Using fingerprint reader: " + readers.get(0).GetDescription().version);
//            System.out.println("Using fingerprint reader: " + readers.get(1).GetDescription().version);
        } catch (UareUException e) {
            showNotification("Error initializing fingerprint reader: " + e.getMessage(),"notificationLabel");
//            System.err.println("Error initializing fingerprint reader: " + e.getMessage());

        }
    }

    public void initializeSignature(){
        try {
            // Load SigPlus Bean
            ClassLoader cl = (com.topaz.sigplus.SigPlus.class).getClassLoader();
            sigObj = (SigPlus) Beans.instantiate(cl, "com.topaz.sigplus.SigPlus");

            if (sigObj != null) {
                System.out.println("SigPlus initialized successfully.");
            } else {
                System.out.println("Failed to initialize SigPlus.");
            }

            // Configure the signature pad
            sigObj.setTabletModel("SignatureGemLCD1X5");
//            sigObj.setTabletLogicalXSize(5000);
//            sigObj.setTabletLogicalYSize(3000);// Set the correct tablet model
            sigObj.setTabletComPort("HID1"); // Use "USB1" or "HID1" based on connection
            sigObj.setTabletState(1); // Enable the signature pad

            sigObj.setImageTransparentMode(false);// Disable transparency
            sigObj.setImageXSize(500);  // Set width
            sigObj.setImageYSize(150);  // Set height
            sigObj.setDisplayPenColor(Color.BLUE);
            sigObj.setImagePenColor(Color.BLUE);
            sigObj.setDisplayPenWidth(5);
            sigObj.setDisplayDisplayMode(1);

            // Create JavaFX SwingNode
            SwingNode swingNode = new SwingNode();
            createSwingContent(swingNode);



            signatureContainer.getChildren().add(swingNode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void initializeReader() {
//        if (reader != null) return; // Prevent multiple initializations
//
//        try {
//            UareUGlobal.DestroyReaderCollection();
//            ReaderCollection readers = UareUGlobal.GetReaderCollection();
//            readers.GetReaders();
//
//            if (!readers.isEmpty()) {
//                reader = readers.get(0);
//                reader.Open(Reader.Priority.EXCLUSIVE);
//                engine = UareUGlobal.GetEngine();
//            }
//        } catch (UareUException e) {
//            showNotification("Error initializing fingerprint reader", "error");
//        }
//    }
//


    private void createSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout()); // Ensure full usage of panel
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            panel.setPreferredSize(new Dimension(400, 200)); // Adjust size
            panel.setBackground(Color.WHITE); // Set background color
            sigObj.repaint();
            panel.add(sigObj, BorderLayout.CENTER); // Add signature panel inside

            swingNode.setContent(panel);

            SwingUtilities.invokeLater(() -> {
                sigObj.repaint();
                panel.revalidate();
                panel.repaint();
            });


        });
    }

    // Capture Signature as Image and Display
    @FXML
    private void captureSignature() {
        int points = sigObj.numberOfTabletPoints();
        System.out.println("Points captured: " + points);

        if (points == 0) {
            System.out.println("No signature detected.");
            return;
        }

        BufferedImage image = sigObj.sigImage();
        if (image == null) {
            System.out.println("Signature image is null. Capture might not be working.");
            return;
        }

        try {
            capturedSignatureBase64 = convertImageToBase64(image);

            // Convert BufferedImage directly to JavaFX Image (no file needed)
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            Image fxImage = new Image(bais);
            signatureImage.setPreserveRatio(true);


// Get image dimensions
            double imgWidth = image.getWidth();
            double imgHeight = image.getHeight();

// Calculate dynamic cropping area
            double cropX = imgWidth * 0.1;  // Adjust X position
            double cropY = imgHeight * 0.1; // Adjust Y position
            double cropWidth = imgWidth * 0.8;
            double cropHeight = imgHeight * 0.8;

// Apply viewport dynamically
            Rectangle2D viewport = new Rectangle2D(cropX, cropY, cropWidth, cropHeight);
            signatureImage.setViewport(viewport);
            signatureImage.setImage(fxImage);
            signaturePane.setVisible(false);
            infoPane.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            sigObj.setTabletState(0);
        }
    }

    @FXML
    private void startSignature(){
        infoPane.setVisible(false);
        signaturePane.setVisible(true);
        signatureBtn.setVisible(false);
        initializeSignature();
    }

    // Save Signature to File
    private void saveSignature() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("signature.sig");
            fileOutputStream.write(sigObj.getSigString().getBytes());
            fileOutputStream.close();
            System.out.println("Signature saved as .sig file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startCameraStream() {

        // Open default camera
        capturing = true;

        captureThread = new Thread(() -> {
            Mat frame = new Mat();
            MatOfByte buffer = new MatOfByte();

            while (capturing) {
                if (capture.read(frame)) {
                    lastFrame = frame.clone();

                    Imgcodecs.imencode(".png", frame, buffer);
                    ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toArray());

                    try {
                        BufferedImage image = ImageIO.read(bis);
                        Image fxImage = SwingFXUtils.toFXImage(image, null);

                        Platform.runLater(() -> cameraPreview.setImage(fxImage));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        captureThread.setDaemon(true);
        captureThread.start();
    }



    private void startExternalCamera(int cameraIndex) {
        new Thread(() -> {
            capture.open(cameraIndex, Videoio.CAP_DSHOW); // Use DirectShow for fast startup
            capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 640); // Reduce resolution for faster startup
            capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);

            try {
                Thread.sleep(500); // Allow camera to initialize
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                if (capture.isOpened()) {
                    System.out.println("External camera started successfully.");
                    startCameraStream();
                } else {
                    showNotification("Failed to open external camera.","notificationLabel");
                    System.out.println("Failed to open external camera.");
                }
            });
        }).start();
    }

    @FXML
    private void startCamera(){

        startExternalCamera(1);
        cameraPane.setVisible(true);
        infoPane.setVisible(false);
        emailPane.setVisible(false);
        loader.setVisible(false);
    }

    @FXML
    private void captureAndShowImage() {
        if (lastFrame != null) {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", lastFrame, buffer);
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(buffer.toArray()));
                capturedPictureBase64 = convertImageToBase64(image);
                File output = new File("captured_image.png");
                ImageIO.write(image, "png", output);
                profileImg.setImage(SwingFXUtils.toFXImage(image, null));
                System.out.println("Image saved: " + output.getAbsolutePath());
                cameraPane.setVisible(false);
                infoPane.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();

            }finally {
                capturing = false;
                if (capture.isOpened()) {
                    capture.release();
                }
            }
        }
    }

    @FXML
    private void cancelCameraUpload() {
        cameraPane.setVisible(false);
        infoPane.setVisible(true);

        capturing = false;
        if (capture.isOpened()) {
            capture.release();
        }
    }


    private void getSeafarer(String emailVal){
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Add proper exception handling for login call
                try {
                    return ApiService.getSeafarer(emailVal);
                } catch (Exception e) {
                    // Handle other exceptions
                    updateMessage("Unexpected error: " + e.getMessage());
                    return null;
                }
            }
        };

        task.setOnSucceeded(ev -> {
            String result = task.getValue();

            if (result != null) {

                try {
                    JsonNode res = objectMapper.readTree(result);
                    if (res.has("seafarer") && !res.get("seafarer").isNull()) {
                        JsonNode seafarer = res.get("seafarer");
                        System.out.println(seafarer);

                        firstName.setText(seafarer.has("firstName") ? seafarer.get("firstName").asText() : "N/A");
                        lastName.setText(seafarer.has("lastName") ? seafarer.get("lastName").asText() : "N/A");
                        seafarerEmail = seafarer.has("email") ? seafarer.get("email").asText() : "N/A";
                        seafarerNo = seafarer.has("id")? seafarer.get("id").asText() : "N/A";

                        System.out.println("seafarer image  =======> " + seafarer.get("seafarer_picture").asText());

                        if (seafarer.has("seafarer_picture") && !seafarer.get("seafarer_picture").asText().isEmpty() && !seafarer.get("seafarer_picture").asText().equals("null")) {

                            System.out.println("Loading picture to profile Image");

                            String base64String = seafarer.get("seafarer_picture").asText();

                           loadProfileImage(base64String);
                        }
                        loader.setVisible(false);
                        emailPane.setVisible(false);
                        infoPane.setVisible(true);
                        biometricBtnContainer.setVisible(true);
                        thumbImageContainer.setVisible(true);
                    } else {
                        loader.setVisible(false);
                        showNotification("Seafarer not found.", "notificationLabel");
                        System.out.println("Seafarer not found.");

                    }

                } catch (JsonProcessingException e) {

                    loader.setVisible(false);
                    showNotification("Failed to connect to the server.", "notificationLabel");
                    throw new RuntimeException(e.getMessage());
                }

            } else {
                showNotification(task.getMessage(), "notificationLabel");
                System.out.println("Task message: " + task.getMessage());
            }
        });

        task.setOnFailed(ev -> {
            // Handle task failure
            showNotification(task.getMessage(), "notificationLabel");
            System.out.println("Task failed: " + task.getMessage());
        });


        new Thread(task).start();
    }

    private void loadProfileImage(String base64String) {
        Task<Image> imageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                byte[] imageBytes = Base64.getDecoder().decode(base64String);
                return new Image(new ByteArrayInputStream(imageBytes));
            }
        };

        imageTask.setOnSucceeded(event -> profileImg.setImage(imageTask.getValue()));
        new Thread(imageTask).start();
    }

    @FXML
    private void getSeafarerByEmail(){

        String email = emailInput.getText();

        System.out.println("email ================================> " + email);
        if(!email.isEmpty()){
            loader.setVisible(true);
            getSeafarer(email.trim());
        }
    }

    private String convertImageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

//    @FXML
//    private void startEnrollment() {
//
//        System.out.println("Starting fingerprint capture...");
//
//        if(rightCapture == 0  && leftCapture == 0){
//            captureMessage("Click Capture to start. Begin with the right thumb");
//        }
//
//        if (reader == null) {
//            initializeReader();
//            System.out.println("No available reader. Cannot start enrollment.");
//            return;
//        }
//
//        if (captureTask != null && captureTask.isRunning()) {
//            captureTask.cancel();
//        }
//
//        captureTask = new CaptureTask(reader, false, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, event -> {
//            if (event.captureResult != null && event.captureResult.image != null) {
//                System.out.println("Fingerprint image captured.");
//            } else if (event.exception != null) {
//                System.err.println("Capture error: " + event.exception.getMessage());
//            } else {
//                System.err.println("Reader status: " + event.readerStatus.status);
//            }
//        });
//
//        captureTask.setOnSucceeded(event -> {
//            Fid capturedImage = captureTask.getValue();
//
//            if (capturedImage == null) {
//                System.err.println("No fingerprint image captured.");
//                return;
//            }
//
//            Platform.runLater(() -> fingerprintImagePane.showImage(capturedImage));
//
//
//        });
//
//        captureTask.setOnFailed(event -> {
//            showNotification(captureTask.getException().getMessage(), "notificationLabel");
//            System.err.println("Capture failed: " + captureTask.getException().getMessage());
//
//        });
//
//
//        new Thread(captureTask).start();
//    }


    @FXML
    private void startEnrollment() {
        System.out.println("Starting fingerprint capture...");

        if (rightCapture == 0 && leftCapture == 0) {
            captureMessage("Click Capture to start. Begin with the right thumb");
        }

        if (reader == null) {
            initializeReader();
            if (reader == null) {
                System.out.println("No available reader. Cannot start enrollment.");
                return;
            }
        }

        // Cancel previous capture task if running
        if (captureTask != null && captureTask.isRunning()) {
            captureTask.cancel();
        }

        // Initialize new capture task
        captureTask = new CaptureTask(reader, false, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, event -> {
            if (event.captureResult != null && event.captureResult.image != null) {
                System.out.println("Fingerprint image captured.");
            } else if (event.exception != null) {
                System.err.println("Capture error: " + event.exception.getMessage());
            } else {
                System.err.println("Reader status: " + event.readerStatus.status);
            }
        });

        // Handle success scenario
        captureTask.setOnSucceeded(event -> {
            Fid capturedImage = captureTask.getValue();
            if (capturedImage == null) {
                System.err.println("No fingerprint image captured.");
                return;
            }
            Platform.runLater(() -> fingerprintImagePane.showImage(capturedImage));
        });

        // Handle failure scenario
        captureTask.setOnFailed(event -> {
            Throwable exception = captureTask.getException();
            if (exception != null) {
                Platform.runLater(() -> showNotification(exception.getMessage(), "notificationLabel"));
                System.err.println("Capture failed: " + exception.getMessage());
            }
        });

        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();  // Restart Executor
        }

     // Execute capture task using ExecutorService to avoid unnecessary thread creation
        executorService.submit(captureTask);
    }


//    @FXML
//    private void saveFingerPrint() {
//        if (captureTask == null || captureTask.getValue() == null) {
//            captureMessage("No fingerprint captured. Please try again.");
//            System.err.println("No fingerprint captured.");
//            return;
//        }
//
//        Fid capturedImage = captureTask.getValue();
//
//        if (rightCapture == 0) {
//            try {
//                rightCaptureFMD = engine.CreateFmd(capturedImage, Fmd.Format.ANSI_378_2004);
//                System.out.println("right capture image ======> " + rightCaptureFMD);
//            } catch (UareUException e) {
//                throw new RuntimeException(e);
//            }
//            captureMessage("Right thumb saved. Capture the left thumb next.");
//
//            thumbFingerPrintImagePaneRight.showImage(capturedImage);
//            rightCapture = 1;
//            System.out.println("Saved right thumb fingerprint.");
//        } else if (leftCapture == 0) {
//
//            try {
//                leftCaptureFMD = engine.CreateFmd(capturedImage, Fmd.Format.ANSI_378_2004);
//                System.out.println("left capture image ======> " + leftCaptureFMD);
//            } catch (UareUException e) {
//                throw new RuntimeException(e);
//            }
//            thumbFingerPrintImagePaneLeft.showImage(capturedImage);
//            leftCapture = 1;
//
//            captureMessage("Left thumb saved. Both thumbs captured.");
//            System.out.println("Saved left thumb fingerprint.");
//        } else {
//
//            captureMessage("Both thumbs already have fingerprints.");
//            System.out.println("Both thumbs already captured.");
//        }
//
//        fingerprintImagePane.clearImage();
//        startEnrollment();
//    }
   @FXML
    private void saveFingerPrint() {
        if (captureTask == null || captureTask.getValue() == null) {
            captureMessage("No fingerprint captured. Please try again.");
            System.err.println("No fingerprint captured.");
            return;
        }

        Fid capturedImage = captureTask.getValue();

        if (rightCapture == 0) {
            rightCaptureFMD = processFingerprint(capturedImage);
            if (rightCaptureFMD != null) {
                thumbFingerPrintImagePaneRight.showImage(capturedImage);
                rightCapture = 1;
                captureMessage("Right thumb saved. Capture the left thumb next.");
                System.out.println("Saved right thumb fingerprint.");
            }
        } else if (leftCapture == 0) {
            leftCaptureFMD = processFingerprint(capturedImage);
            if (leftCaptureFMD != null) {
                thumbFingerPrintImagePaneLeft.showImage(capturedImage);
                leftCapture = 1;
                captureMessage("Left thumb saved. Both thumbs captured.");
                System.out.println("Saved left thumb fingerprint.");
            }
        } else {
            captureMessage("Both thumbs already have fingerprints.");
            System.out.println("Both thumbs already captured.");
        }

        fingerprintImagePane.clearImage();

        // Start enrollment only if at least one thumb is not captured
        if (rightCapture == 0 || leftCapture == 0) {
            startEnrollment();
        }
    }

    /**
     * Processes the captured fingerprint image and generates FMD.
     */
    private Fmd processFingerprint(Fid capturedImage) {
        try {
            return engine.CreateFmd(capturedImage, Fmd.Format.ANSI_378_2004);
        } catch (UareUException e) {
            System.err.println("Fingerprint processing error: " + e.getMessage());
            return null;
        }
    }



    private void captureMessage(String msg){
        Platform.runLater(() -> {
            System.out.println("This method is been called to change text =========> :) for this text ===> " + msg);
            instructionLbl.setText(null);
            instructionLbl.setVisible(false);
            instructionLbl.setVisible(true);  // Force refresh
            instructionLbl.setText(msg);
        });
    }


    @FXML
    private void saveBiometricInfo() {

        String seafarerVal = seafarerID.getText().trim();

        String ninValue = nin.getText().trim();

        if (seafarerVal.isEmpty() || ninValue.isEmpty()) {

            showNotification("All fields are required", "notificationLabel");
            return;
        }
        isLoading(true, "Saving Biometric record ...");

        if (leftCaptureFMD == null) {

            showNotification("Left fingerprint not captured. Please try again.", "notificationLabel");

            return;
        }

        if (rightCaptureFMD == null) {

            showNotification("Right fingerprint not captured. Please try again.", "notificationLabel");

            return;
        }

        byte[] leftFmdBytes = leftCaptureFMD.getData();

        byte[] rightFmdBytes = rightCaptureFMD.getData();

        if (leftFmdBytes == null) {

            showNotification("Left fingerprint not captured. Please try again.", "notificationLabel");

            return;
        }

        if (rightFmdBytes == null) {

            showNotification("Right fingerprint not captured. Please try again.", "notificationLabel");

            return;
        }


        String rightFmdBase64 = Base64.getEncoder().encodeToString(rightFmdBytes);

        String leftFmdBase64 = Base64.getEncoder().encodeToString(leftFmdBytes);

        System.out.println("ID ===> " + seafarerVal + "\n"
                + "nin ====> " + ninValue + "\n"
                + "right ====> " + rightFmdBase64 + "\n"
                + "left ======> " + leftFmdBase64 + "\n"
                + "email ======> " + seafarerEmail
                + "photo =====> " + capturedPictureBase64
                + "signature ===> " + capturedSignatureBase64
        );

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                System.out.println("Calling biometric api to store data =========");
                try {
                    return ApiService.captureBiometric(seafarerNo,seafarerVal, ninValue, rightFmdBase64, leftFmdBase64, seafarerEmail, capturedPictureBase64, capturedSignatureBase64);
                } catch (Exception e) {
                    // Handle other exceptions
                    System.out.println("End biometric call =========");
                    updateMessage("Unexpected error: " + e.getMessage());
                    return null;
                }
            }
        };

        task.setOnSucceeded(ev -> {

            System.out.println("End biometric call =========");

            String result = task.getValue();

            if (result != null) {
                try {
                    JsonNode res = objectMapper.readTree(result);
                    JsonNode capturedBiometric = res.get("message");
                    System.out.println(capturedBiometric);
                    if(capturedBiometric.asText().equals("Biometric saved successfully")){
                        isLoading(false, "");
                        emailInput.setText("");
                        clearInfo();
                        resetImagePreviewPane();
                        showNotification("Biometric saved successful", "notificationLabelSuccess");
                    }else{
                        isLoading(false, "");
                        infoPane.setVisible(true);
                        showNotification("Saving Biometric record failed. Please try again", "notificationLabel");
                    }

                } catch (JsonProcessingException e) {
                    System.out.println("End biometric call =========");
                    isLoading(false, "");
                    infoPane.setVisible(true);
                    showNotification("Saving Biometric record failed. Please try again", "notificationLabel");
                    throw new RuntimeException(e.getMessage());
                }

            } else {
                isLoading(false, "");
                infoPane.setVisible(true);
                showNotification("Saving Biometric record failed. Please try again", "notificationLabel");
                System.out.println("Task message: " + task.getMessage());
            }
        });

        task.setOnFailed(ev -> {
            // Handle task failure
            isLoading(false, "");
            infoPane.setVisible(true);
            showNotification("Saving Biometric record failed. Please try again", "notificationLabel");
            System.out.println("Task failed: " + task.getMessage());
        });


        new Thread(task).start();
    }

    private void isLoading(boolean processing, String msg){
        infoPane.setVisible(false);
     if(processing){
         if(!loader.isVisible()){
             loader.setVisible(true);
         }

         loadingTxt.setText(msg);
     }else {
         loader.setVisible(false);

     }
    }

    @FXML
    private void clearInfo(){
        loader.setVisible(false);
        emailPane.setVisible(true);
        infoPane.setVisible(false);
        biometricBtnContainer.setVisible(false);
        thumbImageContainer.setVisible(false);
        instructionLbl.setVisible(false);
        loader.setVisible(false);
        firstName.setText("");
        lastName.setText("");
        seafarerID.setText("");
        nin.setText("");
        resetImagePreviewPane();
    }

    @FXML
    private void resetImagePreviewPane() {
        fingerprintImagePane.clearImage();
        thumbFingerPrintImagePaneRight.clearImage();
        thumbFingerPrintImagePaneLeft.clearImage();
        rightCapture = 0;
        leftCapture = 0;

    }

    public void showNotification(String messageText, String className) {
        Platform.runLater(() -> {
            Label notificationLabel = new Label(messageText);
            notificationLabel.getStyleClass().add(className);
            StackPane notificationContainer = new StackPane(notificationLabel);
            notificationContainer.setTranslateY(-50); // Position it at the top
            notificationCenter.getChildren().add(notificationContainer);
            StackPane.setAlignment(notificationContainer, Pos.BOTTOM_CENTER);
            // Fade in effect
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), notificationContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            // Automatically remove notification after 3 seconds
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), notificationContainer);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> notificationCenter.getChildren().remove(notificationContainer));
                fadeOut.play();
            }));
            timeline.setCycleCount(1);
            timeline.play();
        });
    }

    // Ensure executor service is properly shut down when application exits
    public void shutdown() {
        executorService.shutdown();
    }


    public void cleanUp() {

        System.out.println("This is closing all application !!!!!!");
        signatureImage.setImage(null);
        if(sigObj != null){
            sigObj.setTabletState( 0 );
            System.out.println("sigObj is closed !!!!!!");
        }

        try {
            UareUGlobal.DestroyReaderCollection();
        } catch (UareUException e) {
            throw new RuntimeException(e);
        }

        try {

            if (reader != null) {
                reader.Close();

            }
        } catch (UareUException e) {
            System.err.println("Error closing reader: " + e.getMessage());
        }
        shutdown();
    }
}
