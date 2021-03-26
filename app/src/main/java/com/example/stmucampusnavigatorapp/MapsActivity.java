// St. Mary's Campus Navigator
// STMUCampusNavigatorApp.app
// Created Jan 12, 2021
// Last Updated March 16, 2021
// Version 1
// Project Team: Amanda Villarreal, Alex Montes, Natalie Rankin, Darren Griffin, Joe Flores, and Dat Trinh
// ------------------------------------------------------------------------------------------------------------------

// IMPORTS AND PACKAGES (DO NOT DELETE)
package com.example.stmucampusnavigatorapp;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.health.SystemHealthManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.stmucampusnavigatorapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import android.app.AlertDialog;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, TaskLoaderCallBack, GoogleMap.OnMarkerClickListener
{

    // GLOBAL WIDGETS AND VARIABLES
    private GoogleMap stmuMap;          // interactive map // test test
    private AutoCompleteTextView stmu_search;   // Search bar text field
    public List<String> lName = new ArrayList<String>();
    public List<CampusLocation> campusLocationsList = new ArrayList<CampusLocation>();   // to hold campus locations
    Polyline directionalPolyline;
    LocationManager locationManager;    // for getting user location
    LocationListener locationListener;  // for getting user location
    public String selectedLocationName;
    public String selectedLocationPhoneNumber;
    public LatLng selectedLocationLatLng;

    // MAP SCREEN METHODS --------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Setting up map screen
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // XML setup
        //campusSearchBar = (EditText) findViewById(R.id.stmu_search);

        //New Search Bar Function
        AutoCompleteTextView editText = findViewById(R.id.stmu_search); //test success
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_list_item_1, lName); //test success
        editText.setAdapter(adapter); //test success
    }

    @Override
    public void onMapReady(GoogleMap googleMap)  // after Map is set up from onCreate()
    {
        System.out.println("App is ready");
        // MAP SET UP
        stmuMap = googleMap;
        initializeCampusLocationsList();
        initializeSearchBar();
        initializeScrollButtons();
        practiceMethod();


        // Limit the map screen to only display St. Mary's
        final LatLngBounds STMU = new LatLngBounds(new LatLng(29.44945207195666, -98.56892350439986), new LatLng(29.454954521268178, -98.56024923502343)); // Create a LatLngBounds that includes St. Mary's University in United States.
        stmuMap.setLatLngBoundsForCameraTarget(STMU); // Constrain the camera target to St. Mary's University.


        //  move the camera to StMU
        LatLng stMarysUniversity = new LatLng(29.4523, -98.5641);
        stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stMarysUniversity, 15f));  // Zoom in on STMU (1 for world, 15 for streets, 20 for buildings)

        // Use intent to signal to system that location is being requested
        Intent intent = getIntent();
        if(intent.getIntExtra("intent value",0) == 0)
        {
            // For obtaining user location
            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener()
            {
                // These methods are required for LocationListener()
                // We don't use them because we have to get user location permission
                @Override
                public void onLocationChanged(Location location){}
                @Override
                public void onStatusChanged(String s, int i, Bundle bundle){}
                @Override
                public void onProviderEnabled(String s){}
                @Override
                public void onProviderDisabled(String s){}
            };

            // Location permissions granted then obtain location and send to initalizeRecenterbutton
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                initalizeRecenterbutton(lastKnownLocation);
            }
        }
    }


    // SETTING UP CAMPUSLOCATIONSLIST METHODS -----------------------------------------------------------------------------------------------------------

    // sets up the campusLocationsList ArrayList by reading from our asset file (By Darren Griffin)
    public void initializeCampusLocationsList()
    {
        System.out.println("In initializeCampusLocationsList");   // for testing purposes

        // read text from asset CampusLocations.txt and store in an object of CampusLocation; add each CampusLocation to campusLocationsList
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("CampusLocations.txt")));
            String line;

            while((line = br.readLine()) != null) // reads each line of the .txt file
            {
                String[] vals = line.split(";"); // stores each line of the .txt file into an array.
                String a = vals[0];
                String[] b = a.split(","); // splits each row into columns and sets each of the values.
                String val0 = b[0];  // locationName
                String val1 = b[1];  // latitude
                String val2 = b[2];  // longitude
                String val3 = b[3];  // phoneNumber
                String val4 = b[4];  // category
                CampusLocation campusLocationObj = new CampusLocation(val0, val1, val2, val3, val4); //Creates new object for each row of the .txt file
                campusLocationsList.add(campusLocationObj); //stores all locations into an ArrayList.
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    // RECENTER BUTTON METHODS ------------------------------------------------------------------------------------------------------------------------

    // When recenter button is pressed, user location will be obtained (by Natalie Rankin)
    private void initalizeRecenterbutton(Location lastKnownLocation)
    {
        Button recenterButton = (Button) findViewById(R.id.recenterButton);
        recenterButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //Checks if location permission is enabled and if so centers map on user location
                centerMapOnLocation(lastKnownLocation,"User Location");
            }
        });
    }

    // Moves map to users location (by Natalie Rankin)
    public void centerMapOnLocation(Location location, String title)
    {
        //If location permissions not enabled, display AlertDialog to user
        if(location == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Permissions Not Enabled");
            builder.setMessage("Please go into your device's settings and enable location permissions");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
            builder.show();
        }
        else  //recenter map
        {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            stmuMap.clear();
            stmuMap.addMarker(new MarkerOptions().position(userLocation).title("You are here!"));
            stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20f));
        }
    }

    // Requests location permissions and calls centerMapOnLocation if permission is granted (by Natalie Rankin)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If permission granted
        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            // Make sure that PERMISSION_GRANTED is true before we ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                // Store location
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"User Location");
            }
        }
    }


    // SEARCH BAR METHODS -----------------------------------------------------------------------------------------------------------------------------

    // initializes and listens for activity in SearchBar (By Amanda Villarreal)
    private void initializeSearchBar() //test success
    {

        for(CampusLocation location : campusLocationsList){
            lName.add(location.getLocationName());
        }

/*
        stmu_search.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override // press enter or search to make a search
            public boolean onEditorAction(TextView v, int searchActionId, KeyEvent searchEvent)
            {
                if((searchActionId == EditorInfo.IME_ACTION_SEARCH) || (searchActionId == EditorInfo.IME_ACTION_DONE)
                        || (searchEvent.getAction() == KeyEvent.ACTION_DOWN) || (searchEvent.getAction() == KeyEvent.KEYCODE_ENTER))
                {
                    searchCampusLocation(campusSearchBar.getText().toString(), false); // This is where we search for campus locations
                }
                return false;
            }
        }); */
    }

    // conducts a search based on user inputted text (By Amanda Villarreal, Darren Griffin, and Alex Montes)
    private void searchCampusLocation(String searchText, boolean searchingByCategory)
    {
        stmuMap.clear(); // empty map of any markers

        // search for a match between the user's inputted string and a campus location
        for(CampusLocation location : campusLocationsList)
        {
            if(searchingByCategory) // selected scrollbar button
            {
                if(location.getCategory().toLowerCase().contains(searchText.toLowerCase()))
                {
                    LatLng locationPosition = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude())); // lat and lng are flipped for some reason
                    stmuMap.addMarker(new MarkerOptions().position(locationPosition).title(location.getLocationName()));
                }
            }
            /*
            else  // searched in search bar
            {
                if (location.getLocationName().toLowerCase().contains(stmu_search.getText().toString().toLowerCase())) {
                    System.out.println(location.toString()); // prints location name when match found (could be multiple matches)
                    LatLng locationPosition = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude())); // lat and lng are flipped for some reason
                    stmuMap.addMarker(new MarkerOptions().position(locationPosition).title(location.getLocationName()));
                }
            }*/
        }
    }


    //CATEGORIES SCROLLBAR METHODS --------------------------------------------------------------------------------------------------------------------

    //Initializes and listens for one of the buttons in the categories bar (By Alex Montes)
    private void initializeScrollButtons()
    {
        Button academicsBttn = (Button) findViewById(R.id.academicsButton);
        Button amenitiesBttn = (Button) findViewById(R.id.amenitiesButton);
        Button athleticsBttn = (Button) findViewById(R.id.athleticsButton);
        Button foodBttn = (Button) findViewById(R.id.foodButton);
        Button gatheringsBttn = (Button) findViewById(R.id.gatheringButton);
        Button libraryBttn = (Button) findViewById(R.id.libraryButton);
        Button parkingBttn = (Button) findViewById(R.id.parkingButton);
        Button sacredBttn = (Button) findViewById(R.id.sacredButton);
        Button safetyBttn = (Button) findViewById(R.id.safetyButton);


        //listens for one of the buttons called and calls onClick()
        academicsBttn.setOnClickListener(this);
        amenitiesBttn.setOnClickListener(this);
        athleticsBttn.setOnClickListener(this);
        foodBttn.setOnClickListener(this);
        gatheringsBttn.setOnClickListener(this);
        libraryBttn.setOnClickListener(this);
        parkingBttn.setOnClickListener(this);
        sacredBttn.setOnClickListener(this);
        safetyBttn.setOnClickListener(this);
    }

    //Function that determines what the specified button does (By Alex Montes)
    @Override
    public void onClick(View bttn)
    {
        //Searches for campus location based on the location category that was selected
        switch (bttn.getId())
        {
            case R.id.academicsButton:
                Button academicsBttn = (Button) findViewById(R.id.academicsButton);
                searchCampusLocation(academicsBttn.getText().toString(), true);
                break;
            case R.id.amenitiesButton:
                Button amenitiesBttn = (Button) findViewById(R.id.amenitiesButton);
                searchCampusLocation(amenitiesBttn.getText().toString(), true);
                break;
            case R.id.athleticsButton:
                Button athleticsBttn = (Button) findViewById(R.id.athleticsButton);
                searchCampusLocation(athleticsBttn.getText().toString(), true);
                break;
            case R.id.foodButton:
                Button foodBttn = (Button) findViewById(R.id.foodButton);
                searchCampusLocation(foodBttn.getText().toString(), true);
                break;
            case R.id.gatheringButton:
                Button gatheringsBttn = (Button) findViewById(R.id.gatheringButton);
                searchCampusLocation(gatheringsBttn.getText().toString(), true);
                break;
            case R.id.libraryButton:
                Button libraryBttn = (Button) findViewById(R.id.libraryButton);
                searchCampusLocation(libraryBttn.getText().toString(), true);
                break;
            case R.id.parkingButton:
                Button parkingBttn = (Button) findViewById(R.id.parkingButton);
                searchCampusLocation(parkingBttn.getText().toString(), true);
                break;
            case R.id.sacredButton:
                Button sacredBttn = (Button) findViewById(R.id.sacredButton);
                searchCampusLocation(sacredBttn.getText().toString(), true);
                break;
            case R.id.safetyButton:
                Button safetyBttn = (Button) findViewById(R.id.safetyButton);
                searchCampusLocation(safetyBttn.getText().toString(), true);
                break;
            default:
                System.out.println("Invalid button press");
        }
    }


    // INFORMATION BAR METHODS------------------------------------------------------------------------------------------------------------------------

    // Change name of location on Information Bar depending on selected marker (by Amanda Villarreal)
    //@Override
    public boolean onMarkerClick(Marker marker)
    {
        TextView informationBarLocationName = (TextView) findViewById(R.id.locationNametextView);  // location's displayed name in Information Bar
        selectedLocationName = marker.getTitle();

        // Find the selected marker's LatLng and phone number, and store the global values for later use
        for(CampusLocation location : campusLocationsList)
        {
            if(location.getLocationName() == selectedLocationName)
            {
                selectedLocationPhoneNumber = location.getPhoneNumber();
                selectedLocationLatLng = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude()));
            }
        }

        System.out.println("Location marker selected");
        informationBarLocationName.setText(selectedLocationName);
        return false;
    }


    // DIRECTIONS METHODS -----------------------------------------------------------------------------------------------------------------------------

    // practice method to invoke a directional polyline (by Amanda Villarreal)
    private void practiceMethod()
    {
        // sample code for when start directions button is pressed
        LatLng starbucks = new LatLng(29.45302,-98.5629);
        LatLng treadaway = new LatLng(29.45499,-98.56301);
        String url = getDirectionsURL(starbucks, treadaway, "walking");
        new FetchURL(MapsActivity.this).execute(url, "walking");
        //onTaskDone();
    }

    // sets up the URL to be sent to google to create directions (by Amanda Villarreal)
    private String getDirectionsURL(LatLng userLocation, LatLng destination, String directionMode)
    {
        // Users current location as a string
        String userLocationString = "origin=" + userLocation.latitude + "," + userLocation.longitude;

        // Destination as a string
        String destinationString = "destination=" + destination.latitude + "," + destination.longitude;

        // Travel mode as a string
        String mode = "mode=" + directionMode;

        // build parameters section of URL
        String parameters = userLocationString + "&" + destinationString + "&" + mode;

        // now building complete URL to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    // Draws polylines(by Amanda Villarreal)
    @Override
    public void onTaskDone(Object... values)
    {
        // create sample polyline
        //PolylineOptions rectOption = new PolylineOptions().add(location1).add(location2).color(1);
        directionalPolyline = stmuMap.addPolyline((PolylineOptions) values[0]);
    }
}