package location;


import org.junit.Test;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

import java.util.List;

public class LocationTest {

    @Test
    public void placesTest(){
        GooglePlaces client = new GooglePlaces("AIzaSyB-N97d_umXxnlkzBSP27do83hYgo1hEeo");

        Param keyword = new Param("keyword");
        keyword.value("Deutsche Bank Filiale");
        List<Place> places = client.getNearbyPlaces(49.425409, 11.079655, 5000, 5, keyword);

        Place deutscheBank = null;
        for (Place place : places) {
            if (place.getName().equals("Deutsche Bank Filiale") || place.getName().equals("Deutsche Bank")) {
                deutscheBank = place;
                break;
            }
        }

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
        }
    }

}
