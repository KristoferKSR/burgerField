package com.burgerfield.burgerJSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONArray;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BurgerRecognizer {

    private static final String API_URL = "https://pplkdijj76.execute-api.eu-west-1.amazonaws.com/prod/recognize";

    public void postImages(List<String> imageUrls) throws URISyntaxException, JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try{

            Map<String,String> params = new LinkedHashMap<>();
            JSONArray urlArray = new JSONArray(imageUrls);
            params.put("\"urls\"", urlArray.toString());

            String correctParams = params.toString().replace("=", ":");
            System.out.println(correctParams);
            URL url1 = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(correctParams);
            osw.flush();
            osw.close();
            System.err.println(connection.getResponseCode());
            System.err.println(connection.getResponseMessage());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

    }

}
