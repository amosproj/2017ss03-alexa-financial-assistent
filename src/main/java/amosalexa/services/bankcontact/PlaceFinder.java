package amosalexa.services.bankcontact;


import com.google.maps.model.LatLng;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.walkercrou.places.*;

import java.util.List;
import java.util.Locale;

public class PlaceFinder {

    /**
     *
     */
    private static final Logger log = LoggerFactory.getLogger(PlaceFinder.class);

    /**
     * maximum place requests results
     */
    private static final int LIMIT = 10;

    /**
     * search for places near latLng
     * @param latLng latitude - longitude
     * @param keywordParams slot value for place search
     * @return List of places
     */
    public static List<Place> findNearbyPlace(LatLng latLng, String... keywordParams){
        GooglePlaces client = new GooglePlaces(GeoCoder.GOOGLE_MAP_API_KEY);

        Param extraParams = new Param("keyword");
        StringBuilder stringBuilder = new StringBuilder();
        // create keyword Params
        for (String keywordParam : keywordParams) {
            stringBuilder.append(keywordParam);
        }
        extraParams.value(stringBuilder.toString());

        return client.getNearbyPlacesRankedByDistance(latLng.lat, latLng.lng, LIMIT, extraParams);
    }

    /**
     * checks list of places for place with opening hours and containing the slot value name
     * @param places list of places
     * @param slotName slot value
     * @return place
     */
    public static Place findOpeningHoursPlace(List<Place> places, String slotName){
        for(Place place : places){
            // get additional place details
            place = place.getDetails();
                for(Hours.Period period : place.getHours().getPeriods()){
                    // place has opening hours
                    if(!period.getOpeningDay().name().isEmpty()) {
                        // place name contains slot value
                        if(place.getName().toLowerCase().contains(slotName.toLowerCase()))
                            return place;
                    }
                }
            }
        return null;
    }

    public static Place findPlace(List<Place> places, String slotValue){
        for(Place place : places){
            if(place.getName().toLowerCase().contains(slotValue.toLowerCase())){
                return place;
            }
        }
        return null;
    }

    /**
     * get the day of the week
     * @param date string date
     * @param locale locale
     * @return weekday
     */
    public static String getWeekday(String date, Locale locale){
        DateTime dateTime = new DateTime(date);
        return dateTime.dayOfWeek().getAsText(locale);
    }

    /**
     * get the opening or closing hours of the place depending of the weekday
     * @param place place
     * @param isOpening opening/closing
     * @param wd weekday
     * @return time
     */
    public static String getHours(Place place, boolean isOpening, String wd){

        String weekday = getWeekday(wd, Locale.ENGLISH);
        for(Hours.Period period : place.getHours().getPeriods()){
            Day day = period.getClosingDay();
            if(isOpening){
                day = period.getOpeningDay();
            }
            if(day.name().toLowerCase().equals(weekday.toLowerCase())){
                return period.getOpeningTime();
            }
        }
        return null;
    }
}
