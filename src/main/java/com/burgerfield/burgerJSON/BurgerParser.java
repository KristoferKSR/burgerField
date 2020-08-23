package com.burgerfield.burgerJSON;

import com.burgerfield.objects.burgerphoto.Item;
import com.burgerfield.objects.burgervenue.JSONResponse;
import com.burgerfield.objects.burgervenue.Venue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class BurgerParser {

    private static final String TARTU_COORDS = "58.38,26.73";
    private static final String TALLINN_COORDS = "59.43,24.75";

    public List<Item> getBurgerImageData(String id) {
        //Could've built the string better
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
            //Smaller response than the venues, broke it down earlier
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
        //Date for foursquare query
        long milliSeconds = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.format(new Date(milliSeconds));
    }

    public List<Venue> getBurgerspots(String query, boolean isSetToTallinn) {
        //Used resttemplate to start with, but wanted to be sure that the bigger JSON queries got parsed as a stream
        String date = getDate();
        URL url = null;
        try {
            if (isSetToTallinn)
                url = new URL("https://api.foursquare.com/v2/venues/search?client_id=HWDO2NOD0GHFABK5DP5CL2JZNBD422RDEWOU1HMWNH2I5NTJ&client_secret=2VMJAR1GIOHL5JILAMQ4QNKOVXLE4BJEV1EZ254LRG0MGH4C&v=" + date + "&limit=10000&ll=" + TALLINN_COORDS + "&categoryId=" + query);
            else
                url = new URL("https://api.foursquare.com/v2/venues/search?client_id=HWDO2NOD0GHFABK5DP5CL2JZNBD422RDEWOU1HMWNH2I5NTJ&client_secret=2VMJAR1GIOHL5JILAMQ4QNKOVXLE4BJEV1EZ254LRG0MGH4C&v=" + date + "&limit=10000&ll=" + TARTU_COORDS + "&categoryId=" + query);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.setRequestProperty("Accept", "application/json");
        try {
            return (tryParsingBurgers(urlConnection.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }

    public List<Venue> tryParsingBurgers(InputStream is) throws IOException {

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(is);
        ObjectMapper mapper = new ObjectMapper();

        JsonToken token = parser.nextToken();

        // Try find at least one object or array.
        while (!JsonToken.START_ARRAY.equals(token) && token != null && !JsonToken.START_OBJECT.equals(token)) {
            parser.nextToken();
        }

        // No content found
        if (token == null) {
            return null;
        }

        while (true) {
            // If the first token is the start of object
            // the response contains only one object (no array)
            // do not try to get the first object from array.
            try {
                if (!JsonToken.START_OBJECT.equals(token)) {
                    token = parser.nextToken();
                }
                if (!JsonToken.START_OBJECT.equals(token)) {
                    break;
                }
                JSONResponse node = mapper.readValue(parser, JSONResponse.class);
                System.out.println(node.getResponse().getVenues());
                return (node.getResponse().getVenues());

            } catch (JsonParseException e) {
                break;
            }
        }
        return null;
    }

}
