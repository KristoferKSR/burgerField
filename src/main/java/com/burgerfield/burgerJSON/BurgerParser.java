package com.burgerfield.burgerJSON;

import com.burgerfield.objects.burgerphoto.Item;
import com.burgerfield.objects.burgervenue.Venue;
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
import java.util.Objects;

public class BurgerParser {

    private static final String TARTU_COORDS = "58.38, 26.73";
    private static final String TALLINN_COORDS = "59.43, 24.75";

    public List<Item> getBurgerImageData(String id) {

        String date = getDate();
        final String uri = "https://api.foursquare.com/v2/venues/" + id + "/photos?callback=jQuery17208000829075989362_" + System.currentTimeMillis() + "&oauth_token=1PWILU4QNZC2FL25NAJRINTBP51HTTKDTS2J1CTVTVJZ12BO&v=" + date + "&_=" + System.currentTimeMillis();

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);
        String correctOutput = Objects.requireNonNull(result).split("\\(")[1];

        return tokenizeImages(correctOutput);

    }

    private java.util.List<Item> tokenizeImages(String output) {

        ObjectMapper mapper = new ObjectMapper();

        System.out.println(output);

        try {
            JSONObject json = new JSONObject(output);
            JSONArray photos = json
                    .getJSONObject("response")
                    .getJSONObject("photos")
                    .getJSONArray("items");

            String jsonInString = photos.toString();
            System.out.println(jsonInString);
            return mapper.readValue(jsonInString, new TypeReference<List<Item>>() {
            });

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String getDate() {
        long milliSeconds = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.format(new Date(milliSeconds));
    }

    public List<Venue> getBurgerspots(String query) {
        String date = getDate();
        final String uri = "https://api.foursquare.com/v2/venues/search?client_id=HWDO2NOD0GHFABK5DP5CL2JZNBD422RDEWOU1HMWNH2I5NTJ&client_secret=2VMJAR1GIOHL5JILAMQ4QNKOVXLE4BJEV1EZ254LRG0MGH4C&v=" + date + "&limit=1000&ll="+TARTU_COORDS+"&query=" + query;
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);

        return tokenizeVenues(result);
    }


    private List<Venue> tokenizeVenues(String output) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            JSONObject json = new JSONObject(output);
            JSONArray venues = json.getJSONObject("response")
                    .getJSONArray("venues");

            String jsonInString = venues.toString();
            return mapper.readValue(jsonInString, new TypeReference<List<Venue>>() {
            });

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public List<Venue> getTallinnBurgerSpots(String query) {
        String date = getDate();
        final String uri = "https://api.foursquare.com/v2/venues/search?client_id=HWDO2NOD0GHFABK5DP5CL2JZNBD422RDEWOU1HMWNH2I5NTJ&client_secret=2VMJAR1GIOHL5JILAMQ4QNKOVXLE4BJEV1EZ254LRG0MGH4C&v=" + date + "&limit=1000&ll="+TALLINN_COORDS+"&query=" + query;
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);

        return tokenizeVenues(result);
    }
}
