package com.salawubabatunde.seafarerbiometric.controllers;

import com.digitalpersona.uareu.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salawubabatunde.seafarerbiometric.MFXResourcesLoader;
import com.salawubabatunde.seafarerbiometric.css.themes.MFXThemeManager;
import com.salawubabatunde.seafarerbiometric.css.themes.Themes;
import com.salawubabatunde.seafarerbiometric.model.Stats;
import com.salawubabatunde.seafarerbiometric.model.UserData;
import com.salawubabatunde.seafarerbiometric.services.ApiService;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;




public class HomeController implements Initializable {

 private final ObjectMapper objectMapper = new ObjectMapper();

@FXML
BorderPane rootPane;


@FXML
private AnchorPane centerPane;

@FXML
private AnchorPane imageContainer;

@FXML
HBox dashboardNav, captureNav, biometricNav;

@FXML
private ImageView logoImage;

@FXML
    private MFXScrollPane scrollPane;

 @FXML
    private StackPane contentStackPane;

@FXML
private ImageView userImage;

@FXML
private Label username;

    private MFXGenericDialog dialogContent;
    private MFXStageDialog dialog;

private LoaderController loaderController;

    List<HBox> menuItems;

private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        menuItems = List.of(dashboardNav, captureNav, biometricNav);

        if (stage == null) {
            System.err.println("Warning: Stage is not set yet. Ensure setStage() is called.");
        }

        loadPage("fxml/dashboard-view.fxml");

        setActiveMenu(dashboardNav, menuItems);

        String fullName = UserData.getInstance().getFirstName() + " " +  UserData.getInstance().getLastName();

        username.setText(fullName);

//        getStats();

         Stats.getInstance().toString();

        System.out.println("Stats =======> " + Stats.getInstance().toString());
        userImage.setImage(new Image(MFXResourcesLoader.load("images/default.jpg"))); // Ensure image is loaded
        Platform.runLater(() ->{
            applyCircularClip(userImage);
        } );

        centerPane.setFocusTraversable(false);
        centerPane.requestFocus();
        centerPane.setOnMouseClicked(event -> rootPane.requestFocus());



          }

private void initLoading(){

//    rootPane = (BorderPane) stage.getScene().getRoot();

    MFXProgressSpinner spinner = new MFXProgressSpinner();

    this.dialogContent = MFXGenericDialogBuilder.build()
            .setContent(spinner)
            .setShowClose(false)
            .setShowMinimize(false)
            .setShowAlwaysOnTop(false)
            .get();

    this.dialog = MFXGenericDialogBuilder.build(dialogContent)
            .toStageDialogBuilder()
            .initOwner(stage)
            .initModality(Modality.APPLICATION_MODAL)
            .setOwnerNode(contentStackPane)
            .setScrimPriority(ScrimPriority.WINDOW)
            .setScrimOwner(true)
            .get();


    dialogContent.getStylesheets().add(MFXResourcesLoader.load("css/style.css"));

}


private void isLoading(){

    initLoading();

    if (dialogContent != null) {
        convertDialogTo("mfx-dialog");
        dialog.showDialog();
    } else {
        System.err.println("Dialog content is not initialized.");
    }
}

    private void convertDialogTo(String styleClass) {
        dialogContent.getStyleClass().removeIf(
                s -> s.equals("mfx-info-dialog") || s.equals("mfx-warn-dialog") || s.equals("mfx-error-dialog")
        );

        if (styleClass != null)
            dialogContent.getStyleClass().add(styleClass);
    }

    public void setStage(Stage stage) {
        System.out.println("Stage set " + stage.getTitle());
        this.stage = stage;

    }



    private void applyCircularClip(ImageView imageView) {
        if (imageView.getImage() == null) {
            System.out.println("No image found! Clip will not be applied.");
            return;
        }


        Platform.runLater(() -> {
            double radius = 21; // Half of 56px
            Circle clip = new Circle(21, 21, 21);
            imageView.setClip(clip);

            System.out.println("Applying circular clip: width=" + imageView.getFitWidth() + ", height=" + imageView.getFitHeight());
        });
    }

    @FXML
    private void loadDashboardFXML() {
        setActiveMenu(dashboardNav, menuItems);
        loadPage("fxml/dashboard-view.fxml");
    }

    @FXML
    private void loadCaptureBiometricFXML() {
        setActiveMenu(captureNav, menuItems);
        loadPage("fxml/captureBiometric.fxml");

    }

    @FXML
    private void biometricVerificationFXML() {
        setActiveMenu(biometricNav, menuItems);
       loadPage("fxml/biometricVerification.fxml");
    }

    @FXML
    private void logoutFXML() {

        try {

            FXMLLoader loader = new FXMLLoader(MFXResourcesLoader.loadURL("fxml/login-view.fxml"));

            Parent loginRoot = loader.load(); // Load first
            LoginController loginController = loader.getController();
            Scene scene = new Scene(loginRoot);
            MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
            stage.setScene(scene);
            stage.setTitle("Login");
            loginController.setStage(stage);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadPage(String url){

        try {

             FXMLLoader loader = new FXMLLoader(MFXResourcesLoader.loadURL(url));
            //AnchorPane captureBiometricContent = loader.load();
            Parent pageContent = loader.load();
            contentStackPane.getChildren().clear();
            contentStackPane.getChildren().add(pageContent);

        } catch (IOException e) {

            e.printStackTrace();
            //loaderController.disMissLoader();
        }

    }

    private void setActiveMenu(HBox selected, List<HBox> menuItems) {

        for (HBox item : menuItems) {
            item.getStyleClass().remove("active");
        }

        selected.getStyleClass().add("active");
        //activeMenuItem = selected;
    }

    public void disMissLoader(){
        if(dialog != null){
            dialog.close();
        }
    }






}
