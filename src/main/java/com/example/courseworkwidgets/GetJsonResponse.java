package com.example.courseworkwidgets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetJsonResponse {
    public static JsonObject get(String link) {
        try {
            //Creates and sends a request
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            //Obtains and fetches the result to the Gson object, which represents the JSON data type
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            connection.disconnect();

            Gson gson = new Gson();
            return gson.fromJson(response.toString(), JsonObject.class);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
