package com.salawubabatunde.seafarerbiometric.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;

public class ApiService {

    private static final String API_URL = "https://seafarer.ns-edms.com.ng/api/admin/login";
    private static final String API_GETSTATS = "https://seafarer.ns-edms.com.ng/api/admin/getStats";
    private static final String API_GETSEAFARERS = "http://localhost:8000/api/admin/getAllSeafarers";
    private static final String API_GETSEAFARER = "http://localhost:8000/api/admin/getSeafarer/";
    private static final String API_CAPTURE_BIOMETRIC = "http://localhost:8000/api/admin/biometric/store";
    private static final String API_GET_BIOMETRICS_DATA = "http://localhost:8000/api/admin/getAllBiometrics";

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String login(String email, String password) {
        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String jsonResponse = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(jsonResponse);
                return jsonNode.toString();
            }

            // Parse the response body to JSON using Jackson
            String jsonResponse = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            // Return the parsed JSON as a string (or process it further if needed)
            return jsonNode.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }



    public static String getStats() {

        Request request = new Request.Builder()
                .url(API_GETSTATS)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Request failed: " + response.body().string();
            }

            // Parse the response body to JSON using Jackson
            String jsonResponse = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            // Return the parsed JSON as a string (or process it further if needed)
            return jsonNode.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    public static String geBiometriData() {

        Request request = new Request.Builder()
                .url(API_GET_BIOMETRICS_DATA)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Request failed: " + response.body().string();
            }

            // Parse the response body to JSON using Jackson
            String jsonResponse = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            // Return the parsed JSON as a string (or process it further if needed)
            return jsonNode.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }



    public static String getSeafarers() {

        Request request = new Request.Builder()
                .url(API_GETSEAFARERS)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Request failed: " + response.body().string();
            }

            // Parse the response body to JSON using Jackson
            String jsonResponse = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            // Return the parsed JSON as a string (or process it further if needed)
            return jsonNode.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }


    public static String getSeafarer(String email) {
        String APIPATH = API_GETSEAFARER + email;

        System.out.println("Api ===============================> "+ APIPATH);
        Request request = new Request.Builder()
                .url(APIPATH)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Request failed: " + response.body().string();
            }

            // Parse the response body to JSON using Jackson
            String jsonResponse = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            // Return the parsed JSON as a string (or process it further if needed)
            return jsonNode.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    public static String captureBiometric(String seafarerNo, String seafarer_id, String nin, String right_thumb, String left_thumb, String email) {
        RequestBody body = new FormBody.Builder()
                .add("seafarerNo", seafarerNo)
                .add("seafarer_id", seafarer_id)
                .add("nin", nin)
                .add("right_thumb_fmd", right_thumb)
                .add("left_thumb_fmd", left_thumb)
                .add("email", email)
                .build();

        Request request = new Request.Builder()
                .url(API_CAPTURE_BIOMETRIC)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String jsonResponse = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(jsonResponse);
                return jsonNode.toString();
            }

            // Parse the response body to JSON using Jackson
            String jsonResponse = response.body().string();

            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            System.out.println(jsonResponse);
            // Return the parsed JSON as a string (or process it further if needed)
            return jsonNode.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }






}