package com.example.parijat.vpom;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        ResultCallback<Status> {
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private TextView textLat, textLong, state;
    private List<Geofence> mGeofenceList;
    private List<LatLng> past = null;
    private MapFragment mapFragment;
    String Path;
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    DatabaseHandler db;
    // Create a Intent send by the notification
    Button start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
//        textLat = (TextView) findViewById(R.id.lat);
//        textLong = (TextView) findViewById(R.id.lon);
//        state =  findViewById(R.id.startGeofence);
        DatabaseHandler db = new DatabaseHandler(this);
        // initialize GoogleMaps
        initGMaps();
        List<GajaBaja> gajabaja = db.getAllContacts();
        createGoogleApi();// create GoogleApiClient
        mGeofenceList = new ArrayList<Geofence>();
        past = new ArrayList<LatLng>();
        //geofencePointsRequest(this, null, null);
        Path = Environment.getExternalStorageDirectory() + "/aa/";
        LocalBroadcastManager lbc = LocalBroadcastManager.getInstance(this);
        GoogleReceiver googleReceiver = new GoogleReceiver(this);
        lbc.registerReceiver(googleReceiver, new IntentFilter("googlegeofence"));
        FloatingActionButton start =  findViewById(R.id.startGeofence);
        //start.setText(mainbutton);
        start.setOnClickListener(startGeo);
        path=new ArrayList<LatLng>();

    }

////////////////////BUTTON LISTENER////////////////////////////BUTTON LISTENER///////////////////////////BUTTON LISTENER//////////////////////////


    //////////////////Create Geofence Button/////////////////////////
    final View.OnClickListener startGeo = new View.OnClickListener() {
        public void onClick(final View v) {

            switch(v.getId()) {
                case R.id.startGeofence:
                    //Inform the user the button1 has been clicked
                    Log.d(TAG,"StartingGeofence");
                    readLocationDatabase();
                    if(mode==true){
                        Log.d(TAG, "inside Geofence");
                        Intent micIntent = new Intent(MainActivity.this, MicActivity.class);
                        int s=555;
                        Log.d(TAG,currentfence);
                        micIntent.putExtra("key", currentfence); //Optional parameters
                        MainActivity.this.startActivity(micIntent);

                    }

                    else if(mode==false){
                        Log.d(TAG, "outside Geofence");
                        String loca=Double.toString( lastLocation.getLatitude());//geoFenceMarker.getPosition()
                        String longitu=Double.toString(lastLocation.getLongitude());
                        String lika=Getid(currentlocation);//geoFenceMarker.getPosition()
                        markerForGeofence(currentlocation);
                        startGeofence(currentlocation, lika);
                        drawGeofence();
                        Intent micIntent1 = new Intent(MainActivity.this, MicActivity.class);
                        micIntent1.putExtra("key", currentfence);
                        Log.d(TAG,currentfence+"which was passed");//Optional parameters
                        MainActivity.this.startActivity(micIntent1);
                    }
                    break;

            }
        }
    };



    /////////////////INTENT RECIEVER AND INITIALIZATION//////////////////// /////////////////INTENT RECIEVER AND INITIALIZATION////////////////////



    static  String mainbutton="Start mic here";
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    // TODO: 21-Nov-17 get mode from here is pendinga
    public static boolean mode=false;

    static class GoogleReceiver extends BroadcastReceiver {

        MainActivity mActivity;

        public GoogleReceiver(Activity activity) {
            mActivity = (MainActivity) activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: recieved"); //Handle the intent here
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            String fenceID=null;
            //intent.getExtras().getString(fenceID);
            if (geofencingEvent.hasError()) {
                Log.d(TAG, "error occored");
                String error = String.valueOf(geofencingEvent.getErrorCode());
                mainbutton ="Error";

            } else {
                //Log.d(TAG, intent.getExtras().getString("fenceID"));//intent handling and function triggering yo
                //Log.d(TAG, intent.getExtras().getString("event"));
                String event = intent.getExtras().getString("event");
                fenceID=intent.getExtras().getString("fenceID");
                Log.d(TAG, event+fenceID);



                if (event=="Entering"){
                    mode=true;

                    currentfence=fenceID;
                    Log.d("mode", fenceID);
                    mainbutton="Listen to this mic";
                }
                else if(event=="Exiting")
                {
                    mode=false;
                    Log.d("mode", "Exited");
                    currentfence=null;
                    mainbutton="Start new mic here";
                }
                //Handle the intent here
                //Log.d(TAG, "onReceive: recieved"+intent.getExtras().getBoolean("key")); //Handle the intent here
                //String str=intent.getExtras().getString("fenceID");
                //Log.d(TAG,"onrecieveeeee"+str);
                // List<String> list=Lists.newArrayList(Splitter.on(" , ").split(string));
//                for(int i=0;i<list.size();i++)
//                { Log.d(TAG,list.get(i));}
                //Log.d(TAG,list.get(1));}
                //Double Latitude = geofencingEvent.getTriggeringLocation().getLatitude();
                //Double Longitude = geofencingEvent.getTriggeringLocation().getLongitude();
//                List <Geofence> getid= geofencingEvent.getTriggeringGeofences();
//                if(getid.size()>0)
//                {for(int i=0; i<getid.size();i++)
//                {   String ye = getid.get(i).getRequestId();
//                    Log.d(TAG, "Latitude  for this geofence is this one" + ye);}
//
//                }

            }

        }
    }

    public static String currentfence=null;
    public void initgeofence() {
        Log.d(TAG, "initializing past geofences");
        DatabaseHandler db = new DatabaseHandler(this);
        List<GajaBaja> yewala = db.getAllContacts();
        for (GajaBaja cn : yewala) {

            Double lati;
            Double longi;
            lati = Double.parseDouble(cn.getLatitude());
            longi = Double.parseDouble(cn.getLongitude());
            String geoid = cn.getGeofenceid();
            LatLng initial = new LatLng(lati, longi);
            String log = "Id:" + cn.getID() + "Latitude: " + cn.getLatitude() + "Longitude: " + cn.getLongitude() + "File_Names:" + cn.getFilename() + "Geofence_id:" + cn.getGeofenceid();
            Log.d("Dekho", log);
            markerForGeofence(initial);
            if (geoFenceMarker != null) {
                startGeofence(initial, geoid);
                drawGeofence();
            }
        }

    }
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
        { return geoFencePendingIntent;}

        Intent intent = new Intent( this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }






    ///////////////////////////SQLITE OPERATIONS////////////////////////////////////////////////SQLITE OPERATIONS/////////////////////////////////////////////

    private void writeDatabase(LatLng latLng, String geoid, String fileList) {
        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;
        String lat = Double.toString(latitude);
        String lon = Double.toString(longitude);
        String nam = " ";
        DatabaseHandler db = new DatabaseHandler(this);
        db.addContact(new GajaBaja(lat, lon, nam, geoid));
        Log.d(TAG, "writeDatabase:" + lat + "   " + lon + "   " + nam + "   " + geoid + "   ");

    }

    static public String newsoundfilenames(){
        Log.d(TAG,"newsoundfilename: generated a new name and passed to sqlable filename");
        String fileName = UUID.randomUUID().toString() + ".3gp";
        return fileName;
    }


    public String  sqlableFilenames(String geoId){
        Log.d(TAG, "sqlableFilenames added string to earier list of dilenames");
        DatabaseHandler db = new DatabaseHandler(this);
        String existingFiles = db.readbygeoid(geoId);
        String newFileName = new String("");
        if (existingFiles == null) {
            newFileName += ",";
            return newFileName;
        } else {
            newFileName += existingFiles+",";
            String Filenam=newFileName+newsoundfilenames();
            return Filenam;
        }

    }



    void readLocationDatabase() {
        Log.d(TAG, "must read now");
        DatabaseHandler db = new DatabaseHandler(this);
        List<GajaBaja> yewala = db.getAllContacts();
        for (GajaBaja cn : yewala) {
            String log = "Id:" + cn.getID() + "Latitude: " + cn.getLatitude() + "Longitude: " + cn.getLongitude() + "File_Names:" + cn.getFilename() + "Geofence_id:" + cn.getGeofenceid();
            Log.d("Dekho", log);
        }
    }

    // TODO: 20-Nov-17 This function gets already stored filenames from the db. these are then added with a new filenmae. this should be be put back in the database. tommorow.
    // TODO: 20-Nov-17 once these filenames are going easily to the database, next step would be to parse them when taking out and populating a string list with them.
    // TODO: 20-Nov-17 ideally, we should be able to supply the media player with this list and it should play sequentially.
    // TODO: 20-Nov-17 strangely, the geofencing stopped working today. Dont know why. I would write this code again tommorow. Make shantanu look at it and debug better.
    // TODO: 20-Nov-17 also,

    // Create GoogleApiClient instance

    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }


    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
        // TODO: 17-Nov-17 save past geofences in a csv file
    }

    //@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate( R.menu.main_menu, menu );
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch ( item.getItemId() ) {
//            case R.id.geofence: {
//                startGeofence();
//                return true;
//            }
//            case R.id.clear: {
//                clearGeofence();
//                return true;
//            }
//        }
//        return super.onOptionsItemSelected(item);
//    }

    ////////////////////////Permission functions/////////////////////////////////////////Permission functions/////////////////////////////////
    private final int REQ_PERMISSION = 999;


    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }


    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }


    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }


    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        // TODO close app and warn user
    }

    ////////////////////////GMAPS INIT//////////////////GMAPS INIT///////////////////////////////////////////////////////////////////////////////////////////////

    // Initialize GoogleMaps
    private void initGMaps() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    LocationManager locManager;

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMapClickListener(this);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // map.setOnMarkerClickListener(this);
    }

    private LocationRequest locationRequest;


    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;


    // Start location Updates
    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
        writeActualLocation(location);
    }


    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
        recoverGeofenceMarker();
        initgeofence();
    }


    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }


    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }


    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
    }

    public LatLng currentlocation;
    private void writeActualLocation(Location location) {
//        textLat.setText( "Lat: " + location.getLatitude() );
//        textLong.setText( "Long: " + location.getLongitude() );

        //start.setText(mainbutton);
        Log.d(TAG, mainbutton);

        //if ()
        currentlocation = new LatLng(location.getLatitude(), location.getLongitude());
        path.add(currentlocation);
        Path(path);

        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }


    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    //////////////////MAP INTERACTIONS/////////////////////MAP INTERACTIONS////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick("+latLng +")");
//        markerForGeofence(latLng);
//        LatLng loca=latLng;//geoFenceMarker.getPosition()
//        String lika=Getid(latLng);//geoFenceMarker.getPosition()
//        startGeofence(loca, lika);
//        drawGeofence();
    }


    //    @Override
//    public boolean onMarkerClick(Marker marker) {
//        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition() );
//        LatLng localmarker = marker.getPosition();
//        String newname= Getid(marker.getPosition());
//        //startGeofence(localmarker, newname);
//        return false;
//    }
    private Marker locationMarker;


    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( map!=null ) {
            if ( locationMarker != null )
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = 16f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }


    private Marker geoFenceMarker;


    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if ( map!=null ) {
            // Remove last geoFenceMarker


            geoFenceMarker = map.addMarker(markerOptions);
            past.add(latLng);
            Log.d(TAG, "marker added");
        }
    }

//    public void geofencePointsRequest(final Context context, Map<String,String> params, final ProgressDialog pDialog){
//        webService = new WebService();
//
//        CustomArrayRequest jsonObjReq = new CustomArrayRequest(
//                DownloadManager.Request.Method.GET,
//                WebServiceUrl.POINTS,
//                params,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        ArrayList<Point> points = webService.getPoints(response);
//                        populateGeofenceList(points);
//                    }
//                },
//
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        String errorHandler = webService.getError(error);
//                        int status = (error.networkResponse.statusCode != 0) ? error.networkResponse.statusCode : 0;
//                        Toast.makeText(context, "Code : " + status + " - Error : " + errorHandler, Toast.LENGTH_LONG).show();
//                    }
//                }) {
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                return webService.getHeaders();
//            }
//        };
//
//        VolleySingleton.getInstance(context).addToRequestQueue("pointsRequest", jsonObjReq);
//    }

    ////////////////////////////////////////GEOFENCE CREATION//////////////////////////////////GEOFENCE CREATION/////////////////////////////////////

    // Start Geofence creation process
    private void startGeofence(LatLng latLng, String name) {


        Log.i(TAG, "startGeofence()");
        if( geoFenceMarker != null ) {

            mGeofenceList.add(createGeofence(latLng, name));
            GeofencingRequest geofenceRequest = getAddGeofencingRequest(mGeofenceList);
            addGeofence( mGeofenceList );

        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }


    public String j;


    public String Getid(LatLng latLng){
        double t=latLng.latitude+latLng.longitude;
        String j;
        j=Double.toString(t);

        j="geo"+j;
        String filenames=sqlableFilenames(j);
        Log.d(TAG,"filename");
        writeDatabase(latLng, j, filenames);
        Log.d(TAG, "Getid: "+j);
        currentfence=j;
        return j;
    }


    private static final long GEO_DURATION = 60 * 60 * 1000;

    private static final float GEOFENCE_RADIUS = 100.0f; // in meters


    public int o=0;

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, String id) {
        Log.d(TAG, "createGeofence");

        return new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(id)

                .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        GEOFENCE_RADIUS
                )
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(10000)
                .build();
    }


    private GeofencingRequest getAddGeofencingRequest(List<Geofence> mGeofenceList) {

        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofences( mGeofenceList)
                .build();
    }


    private void addGeofence(List<Geofence> request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }
//    public void populateGeofenceList(ArrayList<Point> points) {
//        for (int i=0; i<points.size(); i++) {
//            mGeofenceList.add(new Geofence.Builder()
//                    .setRequestId(pins.get(i).getShortId())
//                    .setCircularRegion(
//                            points.get(i).getLocation().getCoordinates().latitude,
//                            points.get(i).getLocation().getCoordinates().longitude,
//                            points.get(i).getLocation().getRadius()
//                    )
//                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
//                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
//                    .build());
//        }
//
//        createGoogleApi();
//    }



    // Add the created GeofenceRequest to the device's monitoring list

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
            saveGeofence();
            //drawGeofence();
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;
    private void drawGeofence() {
        for (int i=0;i<past.size();i++){


            Log.d(TAG, "drawGeofence()"+past.get(i));
            CircleOptions circleOptions = new CircleOptions()
                    .center( past.get(i))
                    .strokeColor(Color.argb(255, 70,70,70))
                    .radius( GEOFENCE_RADIUS );
            geoFenceLimits = map.addCircle( circleOptions );}
    }
    private Circle pathCircle;
    private List<LatLng> path=null;
    private void Path(List<LatLng> loca){
        Log.d(TAG,"making path now");

        for (int i=0;i<path.size();i++){
            CircleOptions circleOptions = new CircleOptions()
                    .center( loca.get(i))
                    .fillColor(Color.argb(255, 89, 0, 224))
                    .strokeWidth(3)
                    .strokeColor(Color.argb(255, 70,70,70))
                    .radius( 10f );
            geoFenceLimits = map.addCircle( circleOptions );
        }

    }











    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

    // Saving GeoFence marker with prefs mng
    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong( KEY_GEOFENCE_LAT, Double.doubleToRawLongBits( geoFenceMarker.getPosition().latitude ));
        editor.putLong( KEY_GEOFENCE_LON, Double.doubleToRawLongBits( geoFenceMarker.getPosition().longitude ));
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );

        if ( sharedPref.contains( KEY_GEOFENCE_LAT ) && sharedPref.contains( KEY_GEOFENCE_LON )) {
            double lat = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LAT, -1 ));
            double lon = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LON, -1 ));
            LatLng latLng = new LatLng( lat, lon );
            markerForGeofence(latLng);
            //drawGeofence();
        }
    }

    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if ( status.isSuccess() ) {
                    // remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if ( geoFenceMarker != null)
            geoFenceMarker.remove();
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
    }


}

