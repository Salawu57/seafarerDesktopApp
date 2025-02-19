package com.salawubabatunde.seafarerbiometric.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salawubabatunde.seafarerbiometric.MFXResourcesLoader;
import com.salawubabatunde.seafarerbiometric.css.themes.MFXThemeManager;
import com.salawubabatunde.seafarerbiometric.css.themes.Themes;
import com.salawubabatunde.seafarerbiometric.model.SeafarerData;
import com.salawubabatunde.seafarerbiometric.model.Stats;
import com.salawubabatunde.seafarerbiometric.model.UserData;
import com.salawubabatunde.seafarerbiometric.services.ApiService;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import io.github.palexdev.materialfx.utils.ToggleButtonsUtil;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private ImageView logoImage;

    @FXML
    private MFXTextField emailField;

    @FXML
    private MFXPasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    HBox notificationCenter;

    @FXML
    private VBox loginForm;

    private Stage modalStage;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LoaderController loaderController;

    private  Stage stage;
    public LoginController() {


    }

    public void setStage(Stage stage) {
        if (stage != null) {
            this.stage = stage;
            System.out.println("Stage successfully set in LoginController: " + stage.getTitle());
        } else {
            System.err.println("Stage is null! Make sure setStage() is called.");
        }
    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            if (loginForm.getScene() != null) {
                stage = (Stage) loginForm.getScene().getWindow(); // Get the stage
                loginForm.prefWidthProperty().bind(stage.widthProperty().multiply(0.35));
            }
        });
        // Handle login button action
//        loginButton.setOnAction(event -> handleLogin());
    }

//  Get stats
    private void getStats(){
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Add proper exception handling for login call
                try {
                    return ApiService.getStats();
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
                    Stats.getInstance().setTotalSeafarer(res.get("seafarerStats").asText());
                    Stats.getInstance().setPendingBiometric(res.get("pendingStats").asText());
                    Stats.getInstance().setCaptureBiometric(res.get("capturedStats").asText());
                    System.out.println(res.get("seafarerStats").asText());

                } catch (JsonProcessingException e) {
                    showNotification("Unable to fetch records. Please check your internet connection");
                    throw new RuntimeException(e);
                }

            } else {
                System.out.println("Task message: " + task.getMessage());
            }
        });

        task.setOnFailed(ev -> {
            // Handle task failure
            System.out.println("Task failed: " + task.getMessage());
        });


        new Thread(task).start();
    }

    private void loading(){
        loaderController = new LoaderController(stage, "Logging in...");
        loaderController.loading();
    }

    private void dismiss(){
        loaderController.disMissLoader();
    }

// Get Seafarers
    private void getSeafarers(){

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Add proper exception handling for login call
                try {
                    return ApiService.getSeafarers();
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
                    JsonNode seafarersList = res.get("seafarers");
                    SeafarerData.getInstance().setSeafarers(seafarersList);
                    System.out.println(seafarersList);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            } else {
                System.out.println("Task message: " + task.getMessage());
            }
        });

        task.setOnFailed(ev -> {
            // Handle task failure
            System.out.println("Task failed: " + task.getMessage());
        });


        new Thread(task).start();
    }

    public void showNotification(String messageText) {
        Platform.runLater(() -> {

            Label notificationLabel = new Label(messageText);
            //notificationLabel.setStyle("-fx-background-color: white; -fx-margin-bottom:10;  -fx-padding: 10px; -fx-font-size: 14px; -fx-background-radius: 10px;");
            notificationLabel.getStyleClass().add("notificationLabel");

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
    private void handleLogin(ActionEvent event) {

        String email = emailField.getText();
        String password = passwordField.getText();
        if(email.isEmpty() || password.isEmpty()) {
            showNotification("All fields are required");
            return;
        }
        loading();
        Task<String> task = new Task<>() {

            @Override
            protected String call() throws Exception {
                // Add proper exception handling for login call
                try {
                    return ApiService.login(email, password);
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

                    System.out.println(res);

                    if(res.get("message").asText().equals("Validation failed") || res.get("message").asText().equals("The provided credentials are incorrect")){
                     loaderController.disMissLoader();
                     showNotification("Invalid email or password");
                     return;

                    }

                    JsonNode userDataResponse = res.get("user");
                    UserData.getInstance().setId(userDataResponse.get("id").asText());
                    UserData.getInstance().setFirstName(userDataResponse.get("firstName").asText());
                    UserData.getInstance().setLastName(userDataResponse.get("lastName").asText());
                    UserData.getInstance().setUsername(userDataResponse.get("username").asText());
                    UserData.getInstance().setEmail(userDataResponse.get("email").asText());
                    UserData.getInstance().setPhoneNo(userDataResponse.get("phoneNo").asText());
                    UserData.getInstance().setOffice_location(userDataResponse.get("office_location").asText());
                    UserData.getInstance().setUser_picture(userDataResponse.get("user_picture").asText());
                    System.out.println(userDataResponse.get("firstName").asText());

                    Node source = (Node) event.getSource();
                    if (source != null) {
                        Stage stage = (Stage) source.getScene().getWindow();
                        if (stage != null) {
                            FXMLLoader loader = new FXMLLoader(MFXResourcesLoader.loadURL("fxml/home.fxml"));

                            Parent dashboardRoot = loader.load(); // Load first

                            HomeController homeController = loader.getController(); // Get the controller after loading


                            Scene scene = new Scene(dashboardRoot);
                            MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
                            stage.setScene(scene);
                            stage.setTitle("Dashboard");
                            homeController.setStage(stage);
                            stage.show();
                            loaderController.disMissLoader();
                        } else {
                            loaderController.disMissLoader();
                            System.err.println("Stage is null. Unable to switch scenes.");
                        }
                    } else {
                        loaderController.disMissLoader();
                        System.err.println("Event source is null. Cannot retrieve stage.");
                    }


                } catch (IOException e) {
                    loaderController.disMissLoader();
                    showNotification(e.getMessage());
                    throw new RuntimeException(e);
                }

            } else {
                loaderController.disMissLoader();
                showNotification("Opps something went wrong. Please try again later");
                System.out.println("Login failed with message: " + task.getMessage());
            }
        });

        task.setOnFailed(ev -> {
            loaderController.disMissLoader();
            showNotification("Opps something went wrong. Please try again later");
            System.out.println("Task failed: " + task.getMessage());
        });


        new Thread(task).start();
 }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getStats();
        getSeafarers();
    }
}
