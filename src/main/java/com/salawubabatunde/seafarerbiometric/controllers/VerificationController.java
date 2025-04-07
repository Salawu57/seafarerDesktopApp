package com.salawubabatunde.seafarerbiometric.controllers;

import com.digitalpersona.uareu.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salawubabatunde.seafarerbiometric.model.Biometric;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;


public class VerificationController implements Initializable {

    @FXML
    private Label loadingTxt;

    @FXML
    HBox notificationCenter;

    @FXML
    private Pane emailPane, infoPane,fingerPrintPreview, signatureContainer;

    @FXML
    MFXTextField emailInput,fullName, seafarerID, phoneNumber, email, dob, pob, officeLocation, town, state, tribe;

    @FXML
    TextArea address;

    @FXML
    ImageView profileImg, signatureView;

    @FXML
    VBox loader;

    private Reader reader;

    private CaptureTask captureTask;
    private Engine engine;
    private FingerprintImagePane fingerprintImagePane;
    private Fmd fingerPrint_FMD;
    private List<Biometric> listOfBiometric;

    private Fmd[] m_fmdArray = null;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        listOfBiometric = new ArrayList<>();
        fingerprintImagePane = new FingerprintImagePane();
        fingerPrintPreview.getChildren().add(fingerprintImagePane);
        initializeReader();
        retrieve();

    }


    @FXML
    private void startVerification() {

        System.out.println("Starting fingerprint capture...");

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

            verifyFingerPrint(capturedImage);



        });

        captureTask.setOnFailed(event -> {
            showNotification(captureTask.getException().getMessage(), "notificationLabel");
            System.err.println("Capture failed: " + captureTask.getException().getMessage());

        });


        new Thread(captureTask).start();
    }

    private void verifyFingerPrint(Fid capturedImage) {

           capturedImage.getViews()[0].getImageData();

         
        try {
                fingerPrint_FMD = engine.CreateFmd(capturedImage, Fmd.Format.ANSI_378_2004);

                int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000;

                 Engine.Candidate[] matches = engine.Identify(fingerPrint_FMD, 0, m_fmdArray, target_falsematch_rate, 1);

            System.out.println("match list -=====> "+matches.length);

            if (matches.length == 1){

                System.out.println("list of bio  ======> " + listOfBiometric.size());
                System.out.println("return index ======> " + matches[0].fmd_index);

                String email = listOfBiometric.get(matches[0].fmd_index).email();

                System.out.println("Match =======> " + email);
                loader.setVisible(true);
                getSeafarer(email);

            }else{
                loader.setVisible(false);
                showNotification("finger print not match", "notificationLabel");
            }


        } catch (UareUException e) {
                throw new RuntimeException(e);
            }

    }

    private Fmd convertFMD(byte[] fmdByte){
        
        try {

            Fmd fmd = UareUGlobal.GetImporter().ImportFmd(fmdByte,Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004 );

            return fmd;

        } catch (UareUException e) {
           showNotification("Error Converting ","notificationLabel");
            throw new RuntimeException(e);
        }
    }

    private void retrieve(){

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Add proper exception handling for login call
                try {
                    return ApiService.geBiometriData();
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
                    if (res.has("biometrics") && !res.get("biometrics").isNull()) {
                        JsonNode biometrics = res.get("biometrics");
                        if(biometrics.isEmpty()){
                            loader.setVisible(false);
                            showNotification("The fingerprint data record is empty. Please capture it before proceeding with verification", "notificationLabel");
                            System.out.println("finger print data is empty.");
                            return;
                        }
                        for(JsonNode biometric : biometrics){
                            System.out.println(biometric.get("id"));
                            System.out.println(biometric.get("email"));
                            System.out.println(biometric.get("right_thumb_fmd"));
                            System.out.println(biometric.get("left_thumb_fmd"));
                            String email = biometric.get("email").asText();
                            byte[] right_fmd_byte =  Base64.getDecoder().decode(biometric.get("right_thumb_fmd").asText());

                            byte[] left_fmd_byte =  Base64.getDecoder().decode(biometric.get("left_thumb_fmd").asText());


                            listOfBiometric.add(new Biometric(email, right_fmd_byte));
                            listOfBiometric.add(new Biometric(email, left_fmd_byte));

                        }
                        m_fmdArray = new Fmd[listOfBiometric.size()];
                        m_fmdArray = listOfBiometric.stream()
                                .flatMap(biometric -> Stream.of(convertFMD(biometric.thumb_fmd())))
                                .toArray(Fmd[]::new);

                        System.out.println("total finger print =======> " + m_fmdArray.length);
                    } else {
                        loader.setVisible(false);
                        showNotification("Unable to load data.", "notificationLabel");
                        System.out.println("Unable to load data.");

                    }

                } catch (JsonProcessingException e) {

                    loader.setVisible(false);
                    showNotification("Unable to load data.", "notificationLabel");
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

            int readerNumber = readers.size() - 1;

            reader = readers.getLast();
            reader.Open(Reader.Priority.EXCLUSIVE);


            engine = UareUGlobal.GetEngine();

            System.out.println("Using fingerprint reader: " + readers.size());
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
                        String firstNameTxt = seafarer.has("firstName") ? seafarer.get("firstName").asText() : "";
                        String otherNameTxt = seafarer.has("other") ? seafarer.get("other").asText() : "";
                        String lastNameTxt = seafarer.has("lastName") ? seafarer.get("lastName").asText() : "";

                        String fullNameTxt = String.join(" ",
                                firstNameTxt,
                                otherNameTxt ,
                                lastNameTxt
                        ).trim();

                        fullName.setText(fullNameTxt);
                        seafarerID.setText(seafarer.has("seafarer_gen_id") ? seafarer.get("seafarer_gen_id").asText() : "N/A");
                        phoneNumber.setText(seafarer.has("telephone_no") ? seafarer.get("telephone_no").asText() : "N/A");
                        email.setText(seafarer.has("email") ? seafarer.get("email").asText() : "N/A");
                        address.setText(seafarer.has("address") ? seafarer.get("address").asText() : "N/A");
                        dob.setText(seafarer.has("date_of_birth") ? seafarer.get("date_of_birth").asText() : "N/A");
                        pob.setText(seafarer.has("place_of_birth") ? seafarer.get("place_of_birth").asText() : "N/A");
                        officeLocation.setText(seafarer.has("office_location") ? seafarer.get("office_location").asText() : "N/A");
                        town.setText(seafarer.has("town") ? seafarer.get("town").asText() : "N/A");
                        state.setText(seafarer.has("state") ? seafarer.get("state").asText() : "N/A");
                        tribe.setText(seafarer.has("tribe") ? seafarer.get("tribe").asText() : "N/A");

                        if (seafarer.has("seafarer_picture") && !seafarer.get("seafarer_picture").asText().isEmpty() && !seafarer.get("seafarer_picture").asText().equals("null")) {
                            String base64String = seafarer.get("seafarer_picture").asText();
                            String imageLink = ApiService.baseUrl+"/images/"+seafarer.get("seafarer_picture").asText();
                            System.out.println("image 1 ======> " + imageLink);
                            try {
//                                byte[] imageBytes = Base64.getDecoder().decode(base64String);
                                Image image = new Image(imageLink);
                                profileImg.setImage(image);
                                profileImg.setFitWidth(157);
                                profileImg.setPreserveRatio(true);
                            } catch (IllegalArgumentException e) {
                                // Handle invalid base64 string (e.g., empty or malformed)
                                showNotification("Invalid image data.", "notificationLabel");
                                System.out.println("Error decoding base64 image: " + e.getMessage());
                            }
                        }

                        if (seafarer.has("signature") && !seafarer.get("signature").asText().isEmpty() && !seafarer.get("signature").asText().equals("null")) {

                            String imageLink = ApiService.baseUrl+"/images/"+seafarer.get("signature").asText();

                            System.out.println("image 2 ======> " + imageLink);
                            try {
//                                byte[] imageBytes = Base64.getDecoder().decode(base64String);
                                Image image = new Image(imageLink);
                                signatureView.setImage(image);
                                //profileImg.setFitWidth(157);
                                signatureView.setPreserveRatio(true);
                            } catch (IllegalArgumentException e) {
                                // Handle invalid base64 string (e.g., empty or malformed)
                                showNotification("Invalid image data.", "notificationLabel");
                                System.out.println("Error decoding base64 image: " + e.getMessage());
                            }
                        }
                        loader.setVisible(false);
                        emailPane.setVisible(false);
                        infoPane.setVisible(true);

                    } else {
                       loader.setVisible(false);
                        showNotification("Seafarer not found.", "notificationLabel");
                        System.out.println("Seafarer not found.");

                    }

                } catch (JsonProcessingException e) {

                  loader.setVisible(false);
                   showNotification("Seafarer not found.Please try again later.", "notificationLabel");
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


    @FXML
    private void resetImagePreviewPane() {
        fingerprintImagePane.clearImage();
        emailPane.setVisible(true);
        infoPane.setVisible(false);
        fullName.clear();
        seafarerID.clear();
        phoneNumber.clear();
        email.clear();
        address.clear();
        dob.clear();
        pob.clear();
        officeLocation.clear();
        town.clear();
        state.clear();
        tribe.clear();
        startVerification();
    }


}
