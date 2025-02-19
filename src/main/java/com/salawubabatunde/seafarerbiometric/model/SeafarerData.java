package com.salawubabatunde.seafarerbiometric.model;


import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.Label;

public class SeafarerData {

    private static SeafarerData instance;

    private JsonNode seafarers;


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
}
