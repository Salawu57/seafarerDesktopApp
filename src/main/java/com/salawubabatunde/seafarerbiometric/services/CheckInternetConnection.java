package com.salawubabatunde.seafarerbiometric.services;

import com.salawubabatunde.seafarerbiometric.model.Stats;

import java.net.HttpURLConnection;
import java.net.URL;

public class CheckInternetConnection {
    private static CheckInternetConnection instance;


    public CheckInternetConnection() {
    }

    public static CheckInternetConnection getInstance() {
        if (instance == null) {

            instance = new CheckInternetConnection();
        }
        return instance;
    }


    public boolean isInternetAvailable() {
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (Exception e) {
            return false;
        }
    }


}
