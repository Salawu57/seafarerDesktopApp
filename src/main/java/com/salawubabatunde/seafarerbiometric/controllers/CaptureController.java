package com.salawubabatunde.seafarerbiometric.controllers;

import com.digitalpersona.uareu.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salawubabatunde.seafarerbiometric.model.SeafarerData;
import com.salawubabatunde.seafarerbiometric.services.ApiService;
import com.salawubabatunde.seafarerbiometric.services.CaptureTask;
import com.salawubabatunde.seafarerbiometric.services.FingerprintImagePane;
import com.salawubabatunde.seafarerbiometric.services.ThumbFingerPrintImagePane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

public class CaptureController implements Initializable {

    @FXML
    private Label instructionLbl;

    @FXML
    private Label loadingTxt;

    @FXML Pane sub_root;

    @FXML
    HBox notificationCenter;

    @FXML
    private Pane fingerPrintPreview, rightThumb, leftThumb, emailPane, infoPane;

    @FXML
    MFXTextField emailInput, firstName, lastName,seafarerID,nin;

    @FXML
    ImageView profileImg;

    @FXML
    VBox loader;

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

    private Fmd leftCaptureFMD, rightCaptureFMD;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HomeController homeController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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
                return;
            }

            reader = readers.get(0);
            reader.Open(Reader.Priority.EXCLUSIVE);


            engine = UareUGlobal.GetEngine();

            System.out.println("Using fingerprint reader: " + reader.GetDescription().name);
        } catch (UareUException e) {
            System.err.println("Error initializing fingerprint reader: " + e.getMessage());

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

                        if (seafarer.has("seafarer_picture") && !seafarer.get("seafarer_picture").asText().isEmpty()) {
                            String base64String = seafarer.get("seafarer_picture").asText();
                            try {
                                byte[] imageBytes = Base64.getDecoder().decode(base64String);
                                Image image = new Image(new ByteArrayInputStream(imageBytes));
                                profileImg.setImage(image);
                                profileImg.setFitWidth(157);
                                profileImg.setPreserveRatio(true);
                            } catch (IllegalArgumentException e) {
                                // Handle invalid base64 string (e.g., empty or malformed)
                                showNotification("Invalid image data.", "notificationLabel");
                                System.out.println("Error decoding base64 image: " + e.getMessage());
                            }
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

    @FXML
    private void getSeafarerByEmail(){

        String email = emailInput.getText();

        System.out.println("email ================================> " + email);
        if(!email.isEmpty()){
            loader.setVisible(true);
            getSeafarer(email.trim());
        }
    }


    @FXML
    private void startEnrollment() {

        System.out.println("Starting fingerprint capture...");

        if(rightCapture == 0  && leftCapture == 0){
            captureMessage("Click Capture to start. Begin with the right thumb");
        }


        if (reader == null) {
            initializeReader();
            System.out.println("No available reader. Cannot start enrollment.");
            return;
        }

        if (captureTask != null && captureTask.isRunning()) {
            captureTask.cancel();
        }

        captureTask = new CaptureTask(reader, false, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, event -> {
            if (event.captureResult != null && event.captureResult.image != null) {
                System.out.println("Fingerprint image captured.");
            } else if (event.exception != null) {
                System.err.println("Capture error: " + event.exception.getMessage());
            } else {
                System.err.println("Reader status: " + event.readerStatus.status);
            }
        });

        captureTask.setOnSucceeded(event -> {
            Fid capturedImage = captureTask.getValue();

            if (capturedImage == null) {
                System.err.println("No fingerprint image captured.");
                return;
            }

            Platform.runLater(() -> fingerprintImagePane.showImage(capturedImage));


        });

        captureTask.setOnFailed(event -> {
            showNotification(captureTask.getException().getMessage(), "notificationLabel");
            System.err.println("Capture failed: " + captureTask.getException().getMessage());

        });


        new Thread(captureTask).start();
    }


    @FXML
    private void saveFingerPrint() {
        if (captureTask == null || captureTask.getValue() == null) {
            captureMessage("No fingerprint captured. Please try again.");
            System.err.println("No fingerprint captured.");
            return;
        }

        Fid capturedImage = captureTask.getValue();

        if (rightCapture == 0) {

            try {
                rightCaptureFMD = engine.CreateFmd(capturedImage, Fmd.Format.ANSI_378_2004);
                System.out.println("right capture image ======> " + rightCaptureFMD);
            } catch (UareUException e) {
                throw new RuntimeException(e);
            }
            captureMessage("Right thumb saved. Capture the left thumb next.");

            thumbFingerPrintImagePaneRight.showImage(capturedImage);
            rightCapture = 1;


            System.out.println("Saved right thumb fingerprint.");
        } else if (leftCapture == 0) {

            try {
                leftCaptureFMD = engine.CreateFmd(capturedImage, Fmd.Format.ANSI_378_2004);
                System.out.println("left capture image ======> " + leftCaptureFMD);
            } catch (UareUException e) {
                throw new RuntimeException(e);
            }
            thumbFingerPrintImagePaneLeft.showImage(capturedImage);
            leftCapture = 1;

            captureMessage("Left thumb saved. Both thumbs captured.");
            System.out.println("Saved left thumb fingerprint.");
        } else {

            captureMessage("Both thumbs already have fingerprints.");
            System.out.println("Both thumbs already captured.");
        }

        fingerprintImagePane.clearImage();
        startEnrollment();
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


        String rightFmdBase64 = rightFmdBytes != null ? Base64.getEncoder().encodeToString(rightFmdBytes) : null;

        String leftFmdBase64 = leftFmdBytes != null ? Base64.getEncoder().encodeToString(leftFmdBytes) : null;

        System.out.println("ID ===> " + seafarerVal + "\n" + "nin ====> " + ninValue + "\n" + "right ====> " + rightFmdBase64 + "\n" + "left ======> " + leftFmdBase64 + "\n" + "email ======> " + seafarerEmail);


        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                System.out.println("Calling biometric api to store data =========");
                try {
                    return ApiService.captureBiometric(seafarerNo,seafarerVal, ninValue, rightFmdBase64, leftFmdBase64, seafarerEmail);
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
            //notificationLabel.setStyle("-fx-background-color: white; -fx-margin-bottom:10;  -fx-padding: 10px; -fx-font-size: 14px; -fx-background-radius: 10px;");
             notificationLabel.getStyleClass().add(className);
            StackPane notificationContainer = new StackPane(notificationLabel);
            //setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 10px; -fx-background-radius: 5px;");
           // notificationContainer.setMaxWidth(300);
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





    public void cleanUp() {

        try {
            UareUGlobal.DestroyReaderCollection();
        } catch (UareUException e) {
            throw new RuntimeException(e);
        }

        try {
//            if (captureTask != null) {
//                captureTask.cancel();
//                captureTask = null;  // Ensure task is null
//            }


            if (reader != null) {
                reader.Close();

            }
        } catch (UareUException e) {
            System.err.println("Error closing reader: " + e.getMessage());
        }
    }
}
