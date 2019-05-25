package com.example.parsestarter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity {

    ListView requestListview;
    ArrayList<String> requests = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    ArrayList<Double> requestLatitudes = new ArrayList<Double>();
    ArrayList<Double> requestLongitudes = new ArrayList<Double>();
    ArrayList<String> usernames = new ArrayList<String>();
    LocationManager locationManager;
    LocationListener locationListener;


    public void updateListview(Location location){
        if (location!=null){
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            final ParseGeoPoint geoPointlocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            query.whereNear("location",geoPointlocation);
            query.whereDoesNotExist("driverUserName");
            query.setLimit(10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        requests.clear();
                        requestLatitudes.clear();
                        requestLongitudes.clear();

                        if (objects.size()>0){
                            for(ParseObject object:objects){
                                ParseGeoPoint requestLocation = (ParseGeoPoint) object.get("location");
                                Double distance = geoPointlocation.distanceInKilometersTo(requestLocation);
                                Double finaldist = (double) Math.round(distance*10)/10;
                                requests.add(finaldist.toString()+" kms");
                                requestLatitudes.add(requestLocation.getLatitude());
                                requestLongitudes.add(requestLocation.getLongitude());
                                usernames.add(object.getString("username"));
                            }

                        }else{
                            requests.add("No requests");
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                    else{
                        e.printStackTrace();
                    }
                }
            });
        }
        else{
            Toast.makeText(this, "idhar hugga", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 5000, 0, locationListener);
                    Location lastKnown1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Log.i("second time","second time "+lastKnown1.toString());
                    updateListview(lastKnown1);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        requestListview = (ListView)findViewById(R.id.requestList);

        requests.add("Getting nearby requests ... ");
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,requests);
        requestListview.setAdapter(arrayAdapter);

        requestListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (ContextCompat.checkSelfPermission(ViewRequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location lastknown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    Toast.makeText(ViewRequestsActivity.this,String.valueOf(requestLongitudes.size())+" "+String.valueOf(requestLatitudes.size())+"  "+lastknown.toString() , Toast.LENGTH_SHORT).show();
                    if (requestLatitudes.size() > position && requestLongitudes.size() > position && lastknown != null && usernames.size()> 0) {
                        Intent intent = new Intent(getApplicationContext(),DriverLocationActivity.class);
                        intent.putExtra("requestlatitude",requestLatitudes.get(position));
                        intent.putExtra("requestlongitude",requestLongitudes.get(position));
                        intent.putExtra("driverlatitude",lastknown.getLatitude());
                        intent.putExtra("driverlongitude",lastknown.getLongitude());
                        intent.putExtra("username",usernames.get(position));
                        startActivity(intent);
                    }
                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("first time","first time");
                updateListview(location);

                ParseUser.getCurrentUser().put("location",new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.i("forth time","forth time");
            if(lastKnown != null){
                Log.i("third time","third time");
                updateListview(lastKnown);
            }
        }

    }
}
