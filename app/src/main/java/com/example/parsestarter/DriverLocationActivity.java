package com.example.parsestarter;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        intent = getIntent();

//        Toast.makeText(this, "coming here", Toast.LENGTH_SHORT).show();
        // Add a marker in Sydney and move the camera
        LatLng driver = new LatLng(intent.getDoubleExtra("driverlatitude",0), intent.getDoubleExtra("driverlongitude",0));
        LatLng requester = new LatLng(intent.getDoubleExtra("requestlatitude",0), intent.getDoubleExtra("requestlongitude",0));

        ArrayList<Marker> markers = new ArrayList<Marker>();
        markers.add(mMap.addMarker(new MarkerOptions().position(driver).title("Driver Location")));
        markers.add(mMap.addMarker(new MarkerOptions().position(requester).title("Requester Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(Marker marker: markers){
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,60);
        mMap.animateCamera(cu);
    }

    public void acceptRequest(View view) {

        ParseQuery<ParseObject> query= ParseQuery.getQuery("Request");
        query.whereEqualTo("username",intent.getStringExtra("username"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if(objects.size()>0){
                        for (ParseObject object:objects){
                            object.put("driverUserName", ParseUser.getCurrentUser().getUsername());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null){
                                        Intent dirintent = new Intent(android.content.Intent.ACTION_VIEW,
                                                Uri.parse("http://maps.google.com/maps?saddr="+intent.getDoubleExtra("driverlatitude",0)+","+intent.getDoubleExtra("driverlongitude",0)+"&daddr="+intent.getDoubleExtra("requestlatitude",0)+","+intent.getDoubleExtra("requestlongitude",0)));
                                        startActivity(dirintent);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }
}
