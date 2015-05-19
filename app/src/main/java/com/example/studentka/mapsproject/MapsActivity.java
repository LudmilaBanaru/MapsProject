package com.example.studentka.mapsproject;

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
import android.util.Log;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.ArrayList;


/**
 * Activité principale de l'application ParkFind qui génère la Google Map et les
 * activités qui lui sont liées.
 *
 * @author Julien Burn & Ludmila Banaru
 *
 */
public class MapsActivity extends ActionBarActivity implements LocationListener, OnMapReadyCallback {

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

    final CharSequence[] items = { "Parking handicapé", "Parking publique", "Parking voie" };
    private Dialog dialogBienvenue;
    private Dialog dialogAbout;
    private Button buttonAbout;
    private Button buttonInfo;
    private MarkerOptions markerOpt;
    private ArrayList<Marker> mArray = new ArrayList<>();
    private int my_previous_selected = -1;

  //  private SupportMapFragment mapFragment;
   // private GoogleApiClient mGoogleApiClient;
   // private LocationRequest mLocationRequest;

    /**
     * Méthode OnCreate où l'on instancie la Google Map avec ses options.
     * Instanciation d'un objet de la classe MyParser pour dessiner les
     * polylines en fonction des coordonnées parsées.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        locM = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        Location myLoc = locM.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        myLat=  myLoc.getLatitude();
        myLong = myLoc.getLongitude();

        new TacheAsynchrone().execute();

        start = new LatLng(myLoc.getLatitude(),myLoc.getLongitude());
        end = new LatLng(myLoc.getLatitude(),myLoc.getLongitude());


        /**
         * On lance la méthode startWindow qui affiche un message de bienvenue
         * au démarrage de l'application
         *
         * @see startWindow()
         * */
        startWindow();

        gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        gMap.setMyLocationEnabled(true);
        gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        gMap.setTrafficEnabled(true);

        GoogleMapOptions mapOptions = new GoogleMapOptions();
        mapOptions.compassEnabled(true)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);

       /* création d'une polyline entre les deux markers.*/
        gd = new GoogleDirection(this);

        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                mDoc = doc;
                gMap.addPolyline(gd.getPolyline(doc, 3, Color.RED));
                gMap.addMarker(new MarkerOptions().position(start)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN)));

                gMap.addMarker(new MarkerOptions().position(end)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN)));
            }
        });
        gd.request(start, end, GoogleDirection.MODE_DRIVING);

        /**
         * Instanciation d'un objet de la classe MyParser
         * On récupère les données parsées dans les arraylist pour créer les markers  sur la Google Map
         * start sera ma géolocalisation et end les coordionées obtenus après avoir parser le XML.
         * @see MyParser
         */

    }

    private class TacheAsynchrone extends AsyncTask<Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... param) {
            Log.d("mytag", "start background...");
            ParserXML file = new ParserXML();
            InputStream in = file.getXmlFromUrl("http://parkingtest1.cfapps.io/GetCoordinate?lat="+String.valueOf(myLat)+"&long="+String.valueOf(myLong)+"&type=Handi");
            //Log.d("mytag", str);
            String str = file.convertStreamToString(in);
            Log.d("mytag", str);
            Document doc = file.getDomElement(str);
            String valueLat = file.getValue(doc.getDocumentElement(), "latitude");
            String valueLong = file.getValue(doc.getDocumentElement(), "longitude");
            Log.d("mytag", valueLat);
            Log.d("mytag", valueLong);
            end = new LatLng(Double.parseDouble(valueLat),
                    Double.parseDouble(valueLong));
            return null;
        }


        protected void onPostExecute(InputStream result) {
            Log.d("mytag", "start onpost...");
            MyParser parser = new MyParser();
            ArrayList<ArrayList<LatLng>> list = parser.getCoordinateArrays(result);

            for (ArrayList<LatLng> arrayList : list) {

                for (LatLng latLong : arrayList) {
                    end = new LatLng(latLong.latitude, latLong.longitude);

                }


            }

        }
    }
        /**
         * Le locationManager locM relance l'opération de recherche de fournisseur de service
         * à la reprise de l'application
         */
        @Override
        public void onResume () {
            super.onResume();
            locM = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            if (locM.isProviderEnabled(LocationManager.GPS_PROVIDER))
                locM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
            locM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
        }

    /**
     * Lorsque l'application est mise en pause (arrière plan), on arrête la
     * recherche de fournisseur de service
     */

    @Override
    public void onPause() {
        super.onPause();
        locM.removeUpdates(this);
    }
    /**
     * Méthode qui gère la localisation. À chaque déplacement de l'utilisateur,
    * cette méthode cherche les nouvelles coordonnées. La caméra se déplacera en
    * fonction de la position
    */
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
    /**
     *Genère un message quand le provider
     *  est disponible
     *
     */

    @Override
    public void onProviderEnabled(String provider) {
        String msg = String.format(getResources().getString(R.string.provider_enabled), provider);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     *Genère un message quand le provider
     * change son status
     *
     */

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
    /**
     *Genère un message quand le provider
     *  n'est pas dispononble
     *
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }
    /**
     * Cette méthode gère la séléction des éléments du menu. Chaque élément ou
     * "item" démarre une action spécifique générée par les différentes méthodes
     *
     * item
     * @return item qui représente un élément du menu
     *
     * aboutWindow()
     * startWindow()
     * openAlertSettings()
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_about:
                aboutWindow();

                break;

            case R.id.menu_help:
                startWindow();

                break;
            case R.id.menu_settings:
              // openAlertSettings(null);

                break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }





  /*  private void openAlertSettings(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MapsActivity.this);

        alertDialogBuilder.setTitle("Choisissez le type de parking");



        alertDialogBuilder.setSingleChoiceItems(items, my_previous_selected,
                new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        my_previous_selected = which;
                        switch (which) {
                            case 0:
                                MyParser parser = new MyParser();
                                AssetManager mng = getAssets();

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

                                break;
                            case 1:
                                for (Marker m : mArray) {

                                    m.remove();
                                }
                                mArray.clear();
                            default:
                                break;
                        }

                    }

                });*/
        // set positive button: Yes message
        /*alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                    }

                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        // montre l'alerte

        alertDialog.show();

    }

    /**
     * Méthode qui gère le dialog de bienvenue au démarrage de l'application
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
     * Méthode qui génère le dialog lorsque "à propos" est cliqué
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



    //private ProgressBar progressBar;
    //private Button button;


    public Document getmDoc() {
        return mDoc;
    }

    public void setmDoc(Document mDoc) {
        this.mDoc = mDoc;
    }
}


