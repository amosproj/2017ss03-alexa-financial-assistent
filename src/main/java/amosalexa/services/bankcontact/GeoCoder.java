package amosalexa.services.bankcontact;


import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class GeoCoder {

    private static final Logger log = LoggerFactory.getLogger(GeoCoder.class);

    public static final String GOOGLE_MAP_API_KEY = "AIzaSyB-N97d_umXxnlkzBSP27do83hYgo1hEeo";
    /**
     *  converts address to geo code to perform place search
     * @param address device address
     * @return LatLng of device
     */
    public static LatLng getLatLng(Address address){

        if(address == null){
            address = new Address();
        }

        GeoApiContext context = new GeoApiContext().setApiKey(GOOGLE_MAP_API_KEY);
        GeocodingResult[] results = null;
        try {
            //log.info("Address: " + encoded);
            results =  GeocodingApi.geocode(context, address.getAddressLine1() + " " + address.getCity() + " " + address.getPostalCode()).await();
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

        assert results != null : "GeoCoder could not convert address to lat lang!";

        for(int i = 0; i < results.length; i++){
            System.out.println(results[i]);
        }

        return results[0].geometry.location;
    }


}
