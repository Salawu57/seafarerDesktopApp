package com.salawubabatunde.seafarerbiometric.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.salawubabatunde.seafarerbiometric.model.Seafarer;
import com.salawubabatunde.seafarerbiometric.model.SeafarerData;
import com.salawubabatunde.seafarerbiometric.model.Stats;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;


import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


    private final ObservableList<Seafarer> list = FXCollections.observableArrayList();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();




    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setupTable();
        updateStats();
        fetchData();
        setupAutoRefresh();


        list.addListener((javafx.collections.ListChangeListener<Seafarer>) change -> {
            System.out.println("changing is happening !!!!");
                  });


    }

    private void setupTable() {
        firstName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        otherName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOtherName()));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        status.setCellFactory(column -> new TableCell<Seafarer, Label>() {
            @Override
            protected void updateItem(Label statusLabel, boolean empty) {
                super.updateItem(statusLabel, empty);
                if (empty || statusLabel == null) {
                    setGraphic(null);
                } else {
                    setGraphic(statusLabel);
                }
            }
        });

        seafarerTable.setItems(list);
    }


    private void fetchData() {
        Task<Void> fetchTask = new Task<>() {
            @Override
            protected Void call() {

                Stats.getInstance().getStats();
                SeafarerData.getInstance().loadSeafarers();

                JsonNode seafarersNode = SeafarerData.getInstance().getSeafarers();


                Map<String, Seafarer> updatedData = new HashMap<>();

                seafarersNode.forEach(seafarerNode -> {
                    String firstName = seafarerNode.has("firstName") ? seafarerNode.get("firstName").asText() : "";
                    String lastName = seafarerNode.has("lastName") ? seafarerNode.get("lastName").asText() : "";
                    String otherName = seafarerNode.has("other") ? seafarerNode.get("other").asText() : "";
                    String biometricStatus = seafarerNode.has("biometric_status") ? seafarerNode.get("biometric_status").asText() : "0";
                    String statusText = Objects.equals(biometricStatus, "0") ? "Not Captured" : "Captured";

                    Seafarer seafarer = new Seafarer(firstName, statusText, otherName, lastName);
                    updatedData.put(firstName + lastName, seafarer);
                });

                Platform.runLater(() -> updateTableData(updatedData));
                return null;
            }
        };

        executorService.submit(fetchTask);
    }


    private void updateTableData(Map<String, Seafarer> updatedData) {
        for (int i = 0; i < list.size(); i++) {
            Seafarer existing = list.get(i);
            String key = existing.getFirstName() + existing.getLastName();

            if (updatedData.containsKey(key)) {
                Seafarer updatedSeafarer = updatedData.get(key);

                // Update only if status has changed
                if (!existing.getStatus().equals(updatedSeafarer.getStatus())) {
                    list.set(i, updatedSeafarer);
                }
                updatedData.remove(key);  // Remove processed item
            }
        }

        // Add new items that were not in the list
        for (Seafarer newSeafarer : updatedData.values()) {
            list.add(newSeafarer);
        }

        seafarerTable.refresh();
    }


//    private void updateTableData(Map<String, Seafarer> updatedData) {
//        list.clear();  // Clear previous data
//
//        for (Seafarer seafarer : updatedData.values()) {
//            list.add(seafarer);
//        }
//
//        seafarerTable.setItems(list);
//        seafarerTable.refresh();
//    }


    private void updateStats() {
        Platform.runLater(() -> {
            captureBiometric.setText(Stats.getInstance().getCaptureBiometric());
            pendingBiometric.setText(Stats.getInstance().getPendingBiometric());
            totalSeafarer.setText(Stats.getInstance().getTotalSeafarer());
        });
    }

    private void setupAutoRefresh() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> fetchData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


}
