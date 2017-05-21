package amosalexa.services.bankcontact;


import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.io.IOException;

public class GeoCoder {

    public static final String GOOGLE_MAP_API_KEY = "AIzaSyB-N97d_umXxnlkzBSP27do83hYgo1hEeo";
    /**
     *  converts address to geo code to perform place search
     * @param address device address
     * @return LatLng of device
     */
    public static LatLng getLatLng(Address address){

        GeoApiContext context = new GeoApiContext().setApiKey(GOOGLE_MAP_API_KEY);
        GeocodingResult[] results = null;
        try {
            results =  GeocodingApi.geocode(context, address.getCity() + " " +
                    address.getPostalCode() + " " + address.getAddressLine1()).await();
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

        assert results != null : "GeoCoder could not convert address to lat lang!";
        return results[0].geometry.location;
    }


}
