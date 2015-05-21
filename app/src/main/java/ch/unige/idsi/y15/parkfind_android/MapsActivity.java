package ch.unige.idsi.y15.parkfind_android;

import android.app.Dialog;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.w3c.dom.Document;
import java.io.InputStream;
/**
 * Main activity of ParkFind application that generates Google Map and
 * activities related to it .
 *
 * @authors Julien Burn & Ludmila Banaru
 *
 **/
public class MapsActivity extends ActionBarActivity implements LocationListener, OnMapReadyCallback,View.OnClickListener {

    private LocationManager locM;
    private GoogleMap gMap;
    public double latitude;
    public double longitude;
    public double altitude;
    public float accuracy;
    public String newStatus;
    private GoogleDirection gd;
    private Document mDoc;
    LatLng end;
    LatLng start;
    Double myLat;
    Double myLong;
    Location myLoc;
    GPSTracker test;
    Button b1,b2,b3;
    String type;
    private Dialog dialogAbout;
    private Button buttonAbout;
    private Button buttonInfo;


    /**
     * OnCreate method where we instantiate the Google Map with its options .
     * Instantiating an object of ParserXML class to draw polylines based on parsed coordinates
     * between two markers.
     *
     **/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        b1 = (Button) findViewById(R.id.btnHandi);
        b2 = (Button) findViewById(R.id.btnPublic);
        b3 = (Button) findViewById(R.id.btnVoie);

        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);


        test = new GPSTracker(this);
        myLoc = test.getLocation();
        myLat = myLoc.getLatitude();
        myLong = myLoc.getLongitude();
        start = new LatLng(myLat, myLong);

        /**
         *It launches startWindow method that displays a welcome message at startup of the application.
         * @see startWindow()
         * */
        startWindow();

        gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        gMap.setMyLocationEnabled(true);
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gMap.setTrafficEnabled(true);

        GoogleMapOptions mapOptions = new GoogleMapOptions();
        mapOptions.compassEnabled(true)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);

        gd = new GoogleDirection(this);



    }
    /**
     * When click on the bottoms: Handi, Publique,Voie
     * It displays a polyline with the nearest parking of our geolocation
     * according to a predefined type: Handi, Publique,Voie
     **/
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnHandi :
                type = "Handi";
                new TacheAsynchrone().execute();
                break;
            case R.id.btnPublic :
                type = "Publique";
                new TacheAsynchrone().execute();
                break;
            case R.id.btnVoie:
                type = "Voie";
                new TacheAsynchrone().execute();
                break;
        }
    }

    /**
     * This class allows us to create in background  the routes between a current position (géolocalisation)
     * of the user and the coordinates of the nearest parking place obtained after parsing the XML
     * document retrieved by the  URL from OpenGeneva.
     *
     */

    private class TacheAsynchrone extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... param) {
            ParserXML file = new ParserXML();
            InputStream in = file.getXmlFromUrl("http://parkfind.cfapps.io/GetCoordinate?lat="
                    + String.valueOf(myLat) + "&long=" + String.valueOf(myLong) + "&type="+type);
            String str = file.convertStreamToString(in);
            Document doc = file.getDomElement(str);
            String valueLat = file.getValue(doc.getDocumentElement(), "latitude");
            String valueLong = file.getValue(doc.getDocumentElement(), "longitude");
            end = new LatLng(Double.parseDouble(valueLat),
                    Double.parseDouble(valueLong));

       /**Creating and adding a polyline
       *between the two markers.
       */


            gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                public void onResponse(String status, Document doc, GoogleDirection gd) {
                    mDoc = doc;

                    gMap.addMarker(new MarkerOptions().position(start)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_GREEN)));

                        gMap.addMarker(new MarkerOptions().position(end)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_GREEN)));

                            gMap.addPolyline(gd.getPolyline(doc, 5, Color.BLUE));
                         }
                    });
                  gd.request(start, end, GoogleDirection.MODE_DRIVING);

            return null;
        }
    }

    /**
     * The LocationManager locM restarts
     * the service provider search operation
     *  at the resumption of the operation
     */
    @Override
    public void onResume() {
        super.onResume();
        locM = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (locM.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
        locM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
    }

    /**
     * When the application is paused ( on the background),
     * the service provider search is stopped.
     */

    @Override
    public void onPause() {
        super.onPause();
        locM.removeUpdates(this);
    }

    /**
     * Method that handles  the user's localization.
     * At each  movement of the user,this method looks for new coordinates.
     * The camera will move in function of this  new position.
     */
    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        accuracy = location.getAccuracy();

        String msg = String.format(getResources().getString(R.string.new_location), latitude,
                longitude, altitude, accuracy);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        gMap.animateCamera(cameraUpdate);
        locM.removeUpdates(this);
    }

    /**
     * Generates a message when the provider
     * is available.
     */

    @Override
    public void onProviderEnabled(String provider) {
        String msg = String.format(getResources().getString(R.string.provider_enabled), provider);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Generates a message when the provider
     * changes its status.
     */

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

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

    /**
     * Generates a message when the provider is not available.
     */


    @Override
    public void onProviderDisabled(String provider) {
        String msg = String.format(getResources().getString(R.string.provider_disabled), provider);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMapReady(GoogleMap arg0) {
        // TODO Auto-generated method stub
    }
    /*
    * Inflate the menu;
     * this adds items to the action bar if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }


    /**
     * This method manages the selection of menu items. Each element or
     * item starts a specific action generated by different methods
     * @return item that represents a menu item
     *
     * aboutWindow()
     * startWindow()
     *
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /*  Handle action bar item clicks here. The action bar will
        * automatically handle clicks on the About/Help button,
        * so long
        * as we specify a parent activity in AndroidManifest.xml.
        */
        switch (item.getItemId()) {
            case R.id.menu_about:
                aboutWindow();
                break;
            case R.id.menu_help:
                startWindow();
                break;
           default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method manages the Welcome dialog window when starting the application.
     */
    private void startWindow() {
        final Dialog dialogBienvenu = new Dialog(this);
        dialogBienvenu.setContentView(R.layout.popup_window_info);
        dialogBienvenu.setTitle("Parkfind");

        TextView txt = (TextView) dialogBienvenu.findViewById(R.id.infoTxtView);
        txt.setText(Html.fromHtml(getString(R.string.Bienvenue)));
        txt.setMovementMethod(ScrollingMovementMethod.getInstance());

        buttonInfo = (Button) dialogBienvenu.findViewById(R.id.buttonInfoClose);
        buttonInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBienvenu.dismiss();

            }
        });

        dialogBienvenu.show();

    }
    /**
     * This method manages the About dialog window when starting the application.
     */
    public void aboutWindow() {

        dialogAbout = new Dialog(this);
        dialogAbout.setContentView(R.layout.about_popup);
        dialogAbout.setTitle("À propos...");

        TextView aboutTxt = (TextView) dialogAbout.findViewById(R.id.aboutView);
        aboutTxt.setText(Html.fromHtml(getString(R.string.About)));

        dialogAbout.show();
        buttonAbout = (Button) dialogAbout.findViewById(R.id.buttonClose);
        buttonAbout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogAbout.dismiss();

            }
        });

    }

   public Document getmDoc() {
        return mDoc;
    }

    public void setmDoc(Document mDoc) {
        this.mDoc = mDoc;
    }
}


