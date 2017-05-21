package amosalexa.services.bankcontact;


import com.google.maps.model.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

import java.util.List;

public class PlaceFinder {

    private static final Logger log = LoggerFactory.getLogger(PlaceFinder.class);

    private static final double RADIUS = 5000d;

    private static final int LIMIT = 5;

    public static Place findNearbyPlace(LatLng latLng, String... keywordParams){
        GooglePlaces client = new GooglePlaces(GeoCoder.GOOGLE_MAP_API_KEY);

        Param extraParams = new Param("keyword");
        StringBuilder stringBuilder = new StringBuilder();
        for (String keywordParam : keywordParams) {
            stringBuilder.append(keywordParam);
        }
        extraParams.value(stringBuilder.toString());

        List<Place> places = client.getNearbyPlaces(latLng.lat, latLng.lng, RADIUS, LIMIT, extraParams);

        log.warn("Find Nearby Place: - Found " + places.size());

        for (Place place : places) {
            log.warn("Places: " + place.getName());
            for (String keywordParam : keywordParams) {
                if (place.getName().toLowerCase().contains(keywordParam.toLowerCase())) {
                    return place;
                }
            }
        }
        return null;
    }

}
