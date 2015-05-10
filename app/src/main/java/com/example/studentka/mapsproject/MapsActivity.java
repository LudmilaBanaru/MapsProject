package com.example.studentka.mapsproject;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback {

    private LocationManager locM;
    private GoogleMap gMap;
    public double latitude;
    public double longitude;
    public double altitude;
    public float accuracy;
    public String newStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        GoogleMapOptions mapOptions = new GoogleMapOptions();


        gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        gMap.setMyLocationEnabled(true);
        gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mapOptions.compassEnabled(true)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);


        //PathParser parser = new PathParser();
       // AssetManager mng = getAssets();



      /* Uri gmmIntentUri = Uri.parse("google.navigation:q=46.1691764,6.1422485");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);*/
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<LatLng."+latitude+">,<LatLng."+longitude+">?q=<latLng"+latitude+">,<latLng"+longitude+">(Marker)"));
       // startActivity(intent);
       /* String labelLocation = "a";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<" + latitude + ">,<" + longitude + ">?q=<" + latitude + ">,<" + longitude + ">(" + labelLocation + ")"));
        startActivity(intent);*/

        MyParser parser = new MyParser();
        AssetManager mng = getAssets();

        try {


            InputStream str = mng.open("xml_test.xml");
            ArrayList<ArrayList<LatLng>> list = parser.getCoordinateArrays(str);
            for (ArrayList<LatLng> arrayList : list) {
                PolylineOptions rectOptions = new PolylineOptions();
                for (LatLng latLong : arrayList) {
                    LatLng temp = new LatLng(latLong.latitude, latLong.longitude);
                    rectOptions.add(temp);
                }
                Polyline polyline = gMap.addPolyline(rectOptions);
                polyline.setWidth(8);
                polyline.setColor(Color.RED);
                gMap.addPolyline(rectOptions);


            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {

            InputStream str = mng.open("xml_test.xml");
            ArrayList<ArrayList<LatLng>> list = parser.getCoordinateArrays(str);

            for (ArrayList<LatLng> arrayList : list) {
                MarkerOptions markerOpt = new MarkerOptions();
                for (LatLng latLng : arrayList) {
                    LatLng tempo = new LatLng(latLng.latitude,latLng.longitude);
                    markerOpt.position(tempo);
                    System.out.println(latLng.latitude + " - " + latLng.longitude);
                }
                gMap.addMarker(markerOpt);

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        locM = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (locM.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
        locM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
        }



    @Override
    public void onPause() {
        super.onPause();
        locM.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        accuracy = location.getAccuracy();

        String msg = String.format( getResources().getString(R.string.new_location), latitude,
                longitude, altitude, accuracy);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        gMap.animateCamera(cameraUpdate);
        locM.removeUpdates(this);
    }


    @Override
    public void onProviderEnabled(String provider) {
        String msg = String.format(getResources().getString(R.string.provider_enabled), provider);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //String newStatus = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                newStatus = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                newStatus = "TEMPORARILY_UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                newStatus = "AVAILABLE";
                break;
        }

        String msg = String.format(
                getResources().getString(R.string.provider_disabled), provider);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onProviderDisabled(String provider) {
        String msg = String.format(getResources().getString(R.string.provider_disabled), provider);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMapReady(GoogleMap arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


