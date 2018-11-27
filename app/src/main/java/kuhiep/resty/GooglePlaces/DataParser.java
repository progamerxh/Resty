package kuhiep.resty.GooglePlaces;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kuhiep.resty.Activity.MainActivity;
import kuhiep.resty.Model.Spot;

public class DataParser {

    private Spot getPlace(JSONObject googlePlaceJson) {
        Spot spotPlace = new Spot();
        String placeName = "--NA--";
        String vicinity = "--NA--";
        String placeId = "";
        Double latitude = 0.;
        Double longitude = 0.;
        float[] distance = new float[3];
        String address = "";
        String rating = "";
        String cuisines = "";
        String priceLevel = "";
        String openingTime = "0:00";
        String closingTime = "23:00";
        String phone = "";
        Boolean openStatus = true;
        String reference = "";
        String photoRef = "";

        Log.d("DataParser", "jsonobject =" + googlePlaceJson.toString());
        JSONArray jsonArray;

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("geometry")) {
                latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            if (!googlePlaceJson.isNull("formatted_address")) {
                address = googlePlaceJson.getString("formatted_address");
            }
            if (!googlePlaceJson.isNull("place_id")) {
                placeId = googlePlaceJson.getString("place_id");
            }
            if (!googlePlaceJson.isNull("rating")) {
                rating = googlePlaceJson.getString("rating");
            }
            if (!googlePlaceJson.isNull("opening_hours")) {
                if (googlePlaceJson.getJSONObject("opening_hours").getString("open_now") == "false")
                    openStatus = false;
            }
            if (!googlePlaceJson.isNull("photos")) {
                jsonArray = googlePlaceJson.getJSONArray("photos");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject getPhotos = jsonArray.getJSONObject(i);
                    photoRef = getPhotos.getString("photo_reference");
                }
            }

            spotPlace = new Spot(placeName, null, rating, vicinity, null, 1, "100", openStatus, openingTime, closingTime, null, Math.random() < 0.5);

            if (address.equals(""))
                spotPlace.setAddress(vicinity);
            spotPlace.setSpotId(placeId);
            if (!photoRef.equals("")) {
                StringBuilder photoPlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
                photoPlaceUrl.append("maxwidth=1080");
                photoPlaceUrl.append("&photoreference=" + photoRef);
                photoPlaceUrl.append("&key=" + MainActivity.API_KEY);
                spotPlace.setSpotImageUrl(photoPlaceUrl.toString());
            } else
                spotPlace.setSpotImageUrl(googlePlaceJson.getString("icon"));
            Location.distanceBetween(MainActivity.latitude, MainActivity.longitude, latitude, longitude, distance);
            spotPlace.setDistance(String.format("%.0f", distance[0]));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return spotPlace;

    }

    private ArrayList<Spot> getPlaces(JSONArray jsonArray) {
        int count = jsonArray.length();
        Spot spot;
        ArrayList<Spot> spotList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                spot = getPlace((JSONObject) jsonArray.get(i));
                spotList.add(spot);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return spotList;
    }

    public ArrayList<Spot> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        Log.d("json data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }
}
