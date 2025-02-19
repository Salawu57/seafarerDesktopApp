package com.salawubabatunde.seafarerbiometric.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.salawubabatunde.seafarerbiometric.model.Seafarer;
import com.salawubabatunde.seafarerbiometric.model.SeafarerData;
import com.salawubabatunde.seafarerbiometric.model.Stats;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;


import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.salawubabatunde.seafarerbiometric.MFXResourcesLoader.loadURL;

public class DashboardController implements Initializable {

    @FXML
    private Label captureBiometric;

    @FXML
    private Label pendingBiometric;

    @FXML
    private Label totalSeafarer;

    @FXML
    private TableColumn<Seafarer, String> firstName;

    @FXML
    private TableColumn<Seafarer, String> lastName;

    @FXML
    private TableColumn<Seafarer, String> otherName;

    @FXML
    private TableView<Seafarer> seafarerTable;

    @FXML
    private TableColumn<Seafarer, Label> status;


    JsonNode seafarersNode = SeafarerData.getInstance().getSeafarers();

    ObservableList<Seafarer> list = FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (seafarersNode != null && seafarersNode.isArray()) {
            seafarersNode.forEach(seafarerNode -> {
                // Extract values from JSON
                String firstName = seafarerNode.has("firstName") ? seafarerNode.get("firstName").asText() : "";
                String lastName = seafarerNode.has("lastName") ? seafarerNode.get("lastName").asText() : "";
                String otherName = seafarerNode.has("other") ? seafarerNode.get("other").asText() : "";
                String biometric_status = seafarerNode.has("biometric_status") ? seafarerNode.get("biometric_status").asText() : "0";

                String status = Objects.equals(biometric_status, "0") ? "Not Capture":"Captured";
                // Create a new Seafarer object and add it to the list
                list.add(new Seafarer(firstName, status, otherName, lastName));
            });
        }

        captureBiometric.setText(Stats.getInstance().getCaptureBiometric());
        pendingBiometric.setText(Stats.getInstance().getPendingBiometric());
        totalSeafarer.setText(Stats.getInstance().getTotalSeafarer());

        firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        otherName.setCellValueFactory(new PropertyValueFactory<>("otherName"));

        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        status.setCellFactory(column -> new TableCell<Seafarer, Label>() {
            @Override
            protected void updateItem(Label statusLabel, boolean empty) {
                super.updateItem(statusLabel, empty);
                if (empty || statusLabel == null) {
                    setGraphic(null);
                } else {
                    setGraphic(statusLabel); // Set the label inside the table cell
                }
            }
        });

        seafarerTable.setItems(list);

    }
}
