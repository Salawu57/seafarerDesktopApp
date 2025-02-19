package com.salawubabatunde.seafarerbiometric.controllers;


import com.salawubabatunde.seafarerbiometric.MFXResourcesLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PageLoaderController {

    @FXML
    private Button closeButton;

    @FXML
    private void closeDialog() {
        // Close the dialog when the close button is clicked
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

       public void displayLoader(Pane rootPane){

        try {
            // Load the dialog layout from the FXML file
            FXMLLoader loader = new FXMLLoader(MFXResourcesLoader.loadURL("fxml/dialogLayout.fxml"));
            StackPane dialogPane = loader.load();

            // Create a semi-transparent overlay
            StackPane overlayPane = new StackPane();
            overlayPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // Dark transparent background
            overlayPane.setPrefSize(rootPane.getWidth(), rootPane.getHeight());
            overlayPane.setAlignment(Pos.CENTER);
            // Add overlay to the main layout
            rootPane.getChildren().add(overlayPane);

            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            Scene dialogScene = new Scene(dialogPane);
            dialogStage.setScene(dialogScene);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.UNDECORATED); // Remove window decorations
            dialogStage.initOwner(rootPane.getScene().getWindow());


            // Get main stage position and size
            Stage primaryStage = (Stage) rootPane.getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

// Center the dialog over the primary stage
            dialogStage.setX(primaryStage.getX() + (primaryStage.getWidth() - dialogPane.getPrefWidth()) / 2);
            dialogStage.setY(primaryStage.getY() + (primaryStage.getHeight() - dialogPane.getPrefHeight()) / 2);

            // When the dialog is closed, remove the overlay
            dialogStage.setOnHidden(event -> rootPane.getChildren().remove(overlayPane));

            // Show the dialog
            dialogStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
