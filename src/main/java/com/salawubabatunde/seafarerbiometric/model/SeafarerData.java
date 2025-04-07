package com.salawubabatunde.seafarerbiometric.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salawubabatunde.seafarerbiometric.services.ApiService;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

public class SeafarerData {

    private static SeafarerData instance;

    private JsonNode seafarers;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public SeafarerData() {
    }

    public static SeafarerData getInstance() {
        if (instance == null) {

            instance = new SeafarerData();
        }
        return instance;
    }

    public JsonNode getSeafarers() {
        return seafarers;
    }

    public void setSeafarers(JsonNode seafarers) {
        this.seafarers = seafarers;
    }



    public void loadSeafarers(){

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
                    setSeafarers(seafarersList);

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



}
