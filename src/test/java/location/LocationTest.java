package location;


import amosalexa.services.bankcontact.Address;
import amosalexa.services.bankcontact.GeoCoder;
import amosalexa.services.bankcontact.PlaceFinder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.walkercrou.places.Hours;
import se.walkercrou.places.Place;

import java.io.IOException;
import java.util.List;

public class LocationTest {


    private static final Logger log = LoggerFactory.getLogger(LocationTest.class);

    @Test
    public void placesTest() throws InterruptedException {
        Address dummyAddress = new Address();
        String slotValue = "Sparkasse";
        LatLng deviceLocation = GeoCoder.getLatLng(dummyAddress);
        List<Place> places = PlaceFinder.findNearbyPlace(deviceLocation, slotValue);

        Place place = PlaceFinder.findOpeningHoursPlace(places, slotValue);

        log.warn("Addresse: " + place.getAddress());

        for(Hours.Period period : place.getHours().getPeriods()){
            log.info("Place: " + period.getOpeningDay());
            log.info("Place: " + period.getOpeningTime());
            log.info("Place: " + period.getClosingTime());
        }
    }

    @Test
    public void geoCodingTest() throws InterruptedException, ApiException, IOException {
        GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyB-N97d_umXxnlkzBSP27do83hYgo1hEeo");
        GeocodingResult[] results =  GeocodingApi.geocode(context,
                "Wölkernstraße 11 90459 Nürnberg").await();
        for(GeocodingResult result : results){
            System.out.println(result.geometry.location.toString());
        }
    }
}
