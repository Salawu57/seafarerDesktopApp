package com.salawubabatunde.seafarerbiometric;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salawubabatunde.seafarerbiometric.controllers.CaptureController;
import com.salawubabatunde.seafarerbiometric.controllers.LoginController;
import com.salawubabatunde.seafarerbiometric.css.themes.MFXThemeManager;
import com.salawubabatunde.seafarerbiometric.css.themes.Themes;
import com.salawubabatunde.seafarerbiometric.model.Stats;
import com.salawubabatunde.seafarerbiometric.services.ApiService;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SeafarerBiometric extends Application {

    private CaptureController captureController;

    @Override
    public void start(Stage primaryStage) throws Exception {

        CSSFX.start();
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());

        primaryStage.setTitle("Seafarer Biometric"); // Set the title BEFORE passing the stage

        FXMLLoader loader = new FXMLLoader(MFXResourcesLoader.loadURL("fxml/login-view.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setStage(primaryStage); // Now primaryStage has a title

        System.out.println("Stage set in SeafarerBiometric: " + primaryStage.getTitle());

        Scene scene = new Scene(root);
        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);

        primaryStage.setScene(scene);
        primaryStage.show();
//        CSSFX.start();
//        Rectangle2D screenBounds =  Screen.getPrimary().getVisualBounds();;
//
//        primaryStage.setWidth(screenBounds.getWidth());
//        primaryStage.setHeight(screenBounds.getHeight());
//        primaryStage.setX(screenBounds.getMinX());
//        primaryStage.setY(screenBounds.getMinY());
//
//
//
//        FXMLLoader loader = new FXMLLoader(MFXResourcesLoader.loadURL("fxml/login-view.fxml"));
//
//        Parent root = loader.load();
//
//        LoginController controller = loader.getController();
//        controller.setStage(primaryStage);
//
//        Scene scene = new Scene(root);
//        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
//
//
//
//        root.setOnMouseReleased(event -> {
//            for (Screen screen : Screen.getScreens()) {
//                if (screen.getBounds().contains(primaryStage.getX(), primaryStage.getY())) {
//                    primaryStage.setX(screen.getBounds().getMinX());
//                    primaryStage.setY(screen.getBounds().getMinY());
//                    primaryStage.setFullScreen(true);
//                    break;
//                }
//            }
//        });
//
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Seafarer Biometric");
//        System.out.println("javafx.runtime.version: " + System.getProperty("javafx.runtime.version"));
//        primaryStage.show();


    }

    @Override
    public void stop() {

        if (captureController != null) {
            captureController.cleanUp();
        }
    }
}
