package com.salawubabatunde.seafarerbiometric.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salawubabatunde.seafarerbiometric.services.ApiService;
import javafx.concurrent.Task;



public class Stats {
    private static Stats instance;

    private String totalSeafarer;
    private String pendingBiometric;
    private String captureBiometric;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Stats() {
    }

    public static Stats getInstance() {
        if (instance == null) {

            instance = new Stats();
        }
        return instance;
    }


    public String getTotalSeafarer() {
        return totalSeafarer;
    }

    public void setTotalSeafarer(String totalSeafarer) {
        this.totalSeafarer = totalSeafarer;
    }

    public String getPendingBiometric() {
        return pendingBiometric;
    }

    public void setPendingBiometric(String pendingBiometric) {
        this.pendingBiometric = pendingBiometric;
    }

    public String getCaptureBiometric() {
        return captureBiometric;
    }

    public void setCaptureBiometric(String captureBiometric) {
        this.captureBiometric = captureBiometric;
    }



    public void getStats(){
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
                    setTotalSeafarer(res.get("seafarerStats").asText());
                    setPendingBiometric(res.get("pendingStats").asText());
                    setCaptureBiometric(res.get("capturedStats").asText());
                    System.out.println(res.get("seafarerStats").asText());

                } catch (JsonProcessingException e) {
                    System.out.println("Unable to fetch records. Please check your internet connection");
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


    @Override
    public String toString() {
        return "Stats{" +
                "totalSeafarer='" + totalSeafarer + '\'' +
                ", pendingBiometric='" + pendingBiometric + '\'' +
                ", captureBiometric='" + captureBiometric + '\'' +
                '}';
    }
}
