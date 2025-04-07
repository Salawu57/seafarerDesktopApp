package com.salawubabatunde.seafarerbiometric.services;

import com.salawubabatunde.seafarerbiometric.model.Stats;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ToastMessage {

    private static ToastMessage instance;

    public ToastMessage() {
    }

    public static ToastMessage getInstance() {
        if (instance == null) {

            instance = new ToastMessage();
        }
        return instance;
    }


    public static void showNotification(String messageText, HBox notificationCenter) {
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

}
