package com.salawubabatunde.seafarerbiometric.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class Seafarer {
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty otherName;
    private final Label status;


    public Seafarer(String firstName, String statusText, String otherName, String lastName) {
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.otherName = new SimpleStringProperty(otherName);

        // Create a label with styling
        this.status = new Label(statusText);
        updateStatusStyle(statusText);
    }


    private void updateStatusStyle(String statusText) {
        if ("Captured".equalsIgnoreCase(statusText)) {
            status.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-background-radius: 20px;");
        } else {
            status.setStyle("-fx-background-color: #fbc02d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-background-radius: 20px;");
        }
    }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty otherNameProperty() { return otherName; }

    // Return the status label
    public Label getStatus() { return status; }
}