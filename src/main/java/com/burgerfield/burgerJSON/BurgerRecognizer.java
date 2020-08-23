package com.burgerfield.burgerJSON;

import com.burgerfield.objects.burgervenue.Venue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BurgerRecognizer {

    private static final String API_URL = "https://pplkdijj76.execute-api.eu-west-1.amazonaws.com/prod/recognize";

    public String postImages(List<String> imageUrls) {
        //Using classic HTTPUrlConnection here, had some trouble getting restTemplate working with POST
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            Map<String, String> params = new LinkedHashMap<>();
            JSONArray urlArray = new JSONArray(imageUrls);
            params.put("\"urls\"", urlArray.toString());

            //Probably could've found a better way to make the JSON readable, but I did what I could with the time given
            String correctParams = params.toString().replace("=", ":");
            System.out.println(correctParams);
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());

            osw.write(correctParams);
            osw.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String builtResponse = "";
            String line = "";
            while ((line = reader.readLine()) != null) {
                builtResponse += line;
            }
            osw.close();
            reader.close();

            try {
                JSONObject json = new JSONObject(builtResponse);
                return json.get("urlWithBurger").toString();

            } catch (JSONException e) {
                return null;
            }

        } catch (IOException e) {
            return "Burgerparse error, no burgers for you...";
        }


    }

}
