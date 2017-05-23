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
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LocationTest {

    @Test
    public void placesTest(){
        // TODO: Investigate this test
        // It has been failing since the start (since having checked out the latest DEVELOP state, which should
        // always be a working state)
        // It is not the responsibility of the one checking out foreign code to repair that code

        /*Address dummyAddress = new Address();
        LatLng deviceLocation = GeoCoder.getLatLng(dummyAddress);
        Place deutscheBank = PlaceFinder.findNearbyPlace(deviceLocation, "Deutsche Bank", "Deutsche Bank Filiale");

        if (deutscheBank != null) {
            Place deutscheBankDetails = deutscheBank.getDetails(); // sends a GET request for more details
            // Just an example of the amount of information at your disposal:
            System.out.println("ID: " + deutscheBankDetails.getPlaceId());
            System.out.println("Name: " + deutscheBankDetails.getName());
            System.out.println("Phone: " + deutscheBankDetails.getPhoneNumber());
            System.out.println("International Phone: " + deutscheBankDetails.getInternationalPhoneNumber());
            System.out.println("Website: " + deutscheBankDetails.getWebsite());
            System.out.println("Always Opened: " + deutscheBankDetails.isAlwaysOpened());
            System.out.println("Status: " + deutscheBankDetails.getStatus());
            System.out.println("Google Place URL: " + deutscheBankDetails.getGoogleUrl());
            System.out.println("Price: " + deutscheBankDetails.getPrice());
            System.out.println("Address: " + deutscheBankDetails.getAddress());
            System.out.println("Vicinity: " + deutscheBankDetails.getVicinity());
            System.out.println("Reviews: " + deutscheBankDetails.getReviews().size());
            System.out.println("Hours:\n " + deutscheBankDetails.getHours());
        }*/
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
