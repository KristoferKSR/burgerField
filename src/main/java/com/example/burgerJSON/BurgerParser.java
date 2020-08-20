package com.example.burgerJSON;

import com.example.burgerphoto.Item;
import com.example.burgervenue.Venue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BurgerParser {

    public List<Item> getBurgerImageData(String id) {

        String date = getDate();
        final String uri = "https://api.foursquare.com/v2/venues/" + id + "/photos?callback=jQuery17208000829075989362_"+ System.currentTimeMillis() + "&oauth_token=1PWILU4QNZC2FL25NAJRINTBP51HTTKDTS2J1CTVTVJZ12BO&v="+date+"&_=" + System.currentTimeMillis();

        RestTemplate restTemplate = new RestTemplate();

        String result = restTemplate.getForObject(uri, String.class);
        String correctOutput = result.split("\\(")[1];

        return tokenizeImages(correctOutput);

    }

    private java.util.List<Item> tokenizeImages(String output){

        ObjectMapper mapper = new ObjectMapper();

        System.out.println(output);

        try {
            JSONObject json = new JSONObject(output);
            JSONArray photos = json
                    .getJSONObject("response")
                    .getJSONObject("photos")
                    .getJSONArray("items");



            // JSON string to Java object
            String jsonInString = photos.toString();
            System.out.println(jsonInString);
            return mapper.readValue(jsonInString, new TypeReference<List<Item>>(){});

            //return mapper.readValue(jsonInString, Venues.class);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String getDate() {
        long milliSeconds = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateString = formatter.format(new Date(milliSeconds));
        return dateString;
    }

    public List<Venue> getBurgerspots(String query)
    {
        String date = getDate();
        final String uri = "https://api.foursquare.com/v2/venues/search?client_id=HWDO2NOD0GHFABK5DP5CL2JZNBD422RDEWOU1HMWNH2I5NTJ&client_secret=2VMJAR1GIOHL5JILAMQ4QNKOVXLE4BJEV1EZ254LRG0MGH4C&v="+date+"&limit=1000&ll=58.3, 26.7&query=" + query;


       /* Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", "HWDO2NOD0GHFABK5DP5CL2JZNBD422RDEWOU1HMWNH2I5NTJ");
        params.put("client_secret", "2VMJAR1GIOHL5JILAMQ4QNKOVXLE4BJEV1EZ254LRG0MGH4C");
        params.put("v", "20200810");
        params.put("ll", "58.378, 26.728");
        params.put("query", "burger"); */


        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);

        return tokenize(result);
    }


    private List<Venue> tokenize(String output){

        ObjectMapper mapper = new ObjectMapper();

        try {
            JSONObject json = new JSONObject(output);
            JSONArray venues = json.getJSONObject("response")
                    .getJSONArray("venues");


            // JSON string to Java object
            String jsonInString = venues.toString();

            return mapper.readValue(jsonInString, new TypeReference<List<Venue>>(){});

            //return mapper.readValue(jsonInString, Venues.class);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
