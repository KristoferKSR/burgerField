package com.example.burgerJSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONArray;
import org.json.JSONException;
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

public class BurgerRecognition {

    public void postImages(List<String> imageUrls) throws URISyntaxException, JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://pplkdijj76.execute-api.eu-west-1.amazonaws.com/prod/recognize";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);



        try{

            //String data = "{\"urls\": [\"https:\\/\\/fastly.4sqi.net\\/img\\/general\\/500x500\\/4495867__HsoPFjbsWlCOjVut_11I_LwSNCnvwqm8R_D7bXiYus.jpg\",\"https:\\/\\/fastly.4sqi.net\\/img\\/general\\/500x500\\/27017031_AJ4HMk0D2A6PD-MZ3plDem1-XAf4cmM4RypgLt6XG1E.jpg\",\"https:\\/\\/fastly.4sqi.net\\/img\\/general\\/500x500\\/65325368_YB6hcqwniT3zdsaIrAN-J15svL79ge-UryrBR-uv1uY.jpg\",\"https:\\/\\/fastly.4sqi.net\\/img\\/general\\/500x500\\/44129463_5x38GamP08MtFrRWvxHW8tSqNOAHvlNIw69unfz8WCw.jpg\",\"https:\\/\\/fastly.4sqi.net\\/img\\/general\\/500x500\\/35080714_ClOv-5jTbZdD-nwf6ca7cF5wy1na4ArkDrDska05E2A.jpg\",\"https:\\/\\/fastly.4sqi.net\\/img\\/general\\/500x500\\/29127797_nwH0-IDgNFfOeR6R3EPBcQ2FhvE9uhpC_d6K92yFgvg.jpg\"]}";
            Map<String,String> params = new LinkedHashMap<>();
            JSONArray urlArray = new JSONArray(imageUrls);
            params.put("\"urls\"", urlArray.toString());

            String correctParams = params.toString().replace("=", ":");
            System.out.println(correctParams);
            URL url1 = new URL("https://pplkdijj76.execute-api.eu-west-1.amazonaws.com/prod/recognize");
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

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

    }

}
