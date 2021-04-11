// St. Mary's Campus Navigator

// STMUCampusNavigatorApp.app
// Created Jan 12, 2021
// Last Updated March 29, 2021
// Version 1
// Project Team: Amanda Villarreal, Alex Montes, Natalie Rankin, Darren Griffin, Joe Flores, and Dat Trinh
// ------------------------------------------------------------------------------------------------------------------

// IMPORTS AND PACKAGES (DO NOT DELETE)
package com.example.stmucampusnavigatorapp;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.health.SystemHealthManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

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
    private GoogleMap stmuMap;                  // interactive map
    LatLng stMarysUniversity = new LatLng(29.4523, -98.5641);
    private AutoCompleteTextView stmu_search;   // Search bar text field
    TextView informationBarLocationName;        // Location name displayed in Information Bar
    private List<String> locationNameList = new ArrayList<String>();              // what is this for?
    private List<CampusLocation> campusLocationsList = new ArrayList<CampusLocation>();   // to hold campus locations
    Polyline directionalPolyline;        // draws directions
    LocationManager locationManager;     // for getting user location
    LocationListener locationListener;   // for getting user location
    private String selectedLocationName;          // globals for information bar
    private String selectedLocationPhoneNumber;   // globals for information bar
    private LatLng selectedLocationLatLng;        // globals for information bar
    private BottomSheetBehavior informationBarBehavior;
    Location userCurrentLocation;        // for use in directions Button

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

        //initialize and hide information bar when app first runs
        View bottomSheet = findViewById(R.id.informationBar);
        informationBarBehavior = BottomSheetBehavior.from(bottomSheet);
        setInfoBarState("collapse");
    }

    @Override
    public void onMapReady(GoogleMap googleMap)  // after Map is set up from onCreate()
    {
        System.out.println("App is ready");
        // MAP SET UP
        stmuMap = googleMap;
        informationBarLocationName = (TextView) findViewById(R.id.locationNametextView);  // initialize location's displayed name in Information Bar
        initializeCampusLocationsList();
        initializeSearchBar();
        initializeScrollButtons();
        initializeMarkerListener();
        initializeUPDButton();
        initializeDirectionsButton();
        initializeCallButton();
        //initializeStartButton();

        // Limit the map screen to only display St. Mary's
        final LatLngBounds STMU = new LatLngBounds(new LatLng(29.44945207195666, -98.56892350439986), new LatLng(29.454954521268178, -98.56024923502343)); // Create a LatLngBounds that includes St. Mary's University in United States.
        stmuMap.setLatLngBoundsForCameraTarget(STMU); // Constrain the camera target to St. Mary's University.


        //  move the camera to StMU
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
                userCurrentLocation = lastKnownLocation;
                System.out.print("Your location: ");
                System.out.println(userCurrentLocation);
                initalizeRecenterbutton(lastKnownLocation);
            }
        }
    }


    // SETTING UP CAMPUSLOCATIONSLIST METHODS -----------------------------------------------------------------------------------------------------------

    // sets up the campusLocationsList ArrayList by reading from our asset file (By Darren Griffin)
    public void initializeCampusLocationsList()
    {
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
        Button recenterButton = findViewById(R.id.recenterButton);
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
            stmuMap.addMarker(new MarkerOptions().position(userLocation).title("You are here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
            stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f));
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

    // initializes and listens for activity in SearchBar (By Amanda Villarreal, Alex Montes, and Darren Griffen)
    private void initializeSearchBar() //test success
    {
        //New Search Bar Function
        stmu_search = findViewById(R.id.stmu_search);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_list_item_1, locationNameList); //test success
        stmu_search.setAdapter(adapter); //test success

        //Darren Comment
        for(CampusLocation location : campusLocationsList)
        {
            locationNameList.add(location.getLocationName());
        }


        //Listens for key press
        stmu_search.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                //if key press is a search
                if((actionId == EditorInfo.IME_ACTION_SEARCH) || (actionId == EditorInfo.IME_ACTION_DONE)
                        || (event.getAction() == KeyEvent.ACTION_DOWN) || (event.getAction() == KeyEvent.KEYCODE_ENTER))
                {
                    setInfoBarState("collapse");
                    hideKeyboard();
                    stmu_search.dismissDropDown(); //get rid of drop down bar
                    searchCampusLocation(stmu_search.getText().toString(), false); //search for campus locations
                }
                return false;
            }
        });

        //listens for suggestion clicks
        stmu_search.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                // store location name globally
                selectedLocationName = adapter.getItem(position);
                informationBarLocationName.setText(selectedLocationName);

                // Find the selected drop-down's LatLng and phone number, and store the global values for later use
                for(CampusLocation location : campusLocationsList)
                {
                    if(location.getLocationName() == selectedLocationName)
                    {
                        selectedLocationPhoneNumber = location.getPhoneNumber();
                        selectedLocationLatLng = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude()));
                        stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocationLatLng, 17f));
                    }
                }
                hideKeyboard();
                setInfoBarState("expand");
                searchCampusLocation(adapter.getItem(position), false); //search for specific location
            }
        });
    }

    // conducts a search based on user inputted text (By Amanda Villarreal, Darren Griffin, and Alex Montes)
    private void searchCampusLocation(String searchText, boolean searchingByCategory)
    {
        stmuMap.clear(); // empty map of all markers
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
            else  // searched in search bar
            {
                if (location.getLocationName().toLowerCase().contains(searchText.toLowerCase()))
                {
                    LatLng locationPosition = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude())); // lat and lng are flipped for some reason
                    stmuMap.addMarker(new MarkerOptions().position(locationPosition).title(location.getLocationName())).showInfoWindow();
                }
            }
        }
    }

    // hides the soft keyboard (Implemented by Amanda Villarreal, method written by a user on GeeksforGeeks)
    public void hideKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null)
        {
            // now assign the system service to InputMethodManager
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //CATEGORIES SCROLLBAR METHODS --------------------------------------------------------------------------------------------------------------------

    //Initializes and listens for one of the buttons in the categories bar (By Alex Montes)
    private void initializeScrollButtons()
    {
        Button academicsBttn  = findViewById(R.id.academicsButton);
        Button amenitiesBttn  = findViewById(R.id.amenitiesButton);
        Button athleticsBttn  = findViewById(R.id.athleticsButton);
        Button foodBttn       = findViewById(R.id.foodButton);
        Button gatheringsBttn = findViewById(R.id.gatheringButton);
        Button libraryBttn    = findViewById(R.id.libraryButton);
        Button parkingBttn    = findViewById(R.id.parkingButton);
        Button residenceBttn  = findViewById(R.id.residenceButton);
        Button sacredBttn     = findViewById(R.id.sacredButton);
        Button safetyBttn     = findViewById(R.id.safetyButton);


        //listens for one of the buttons called and calls onClick()
        academicsBttn.setOnClickListener(this);
        amenitiesBttn.setOnClickListener(this);
        athleticsBttn.setOnClickListener(this);
        foodBttn.setOnClickListener(this);
        gatheringsBttn.setOnClickListener(this);
        libraryBttn.setOnClickListener(this);
        parkingBttn.setOnClickListener(this);
        residenceBttn.setOnClickListener(this);
        sacredBttn.setOnClickListener(this);
        safetyBttn.setOnClickListener(this);
    }

    //Function that determines what the specified button does (By Alex Montes)
    @Override
    public void onClick(View bttn)
    {
        setInfoBarState("collapse"); //hide info bar
        stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stMarysUniversity, 15f));
        //Searches for campus location based on the location category that was selected
        switch (bttn.getId())
        {
            case R.id.academicsButton:
                Button academicsBttn = findViewById(R.id.academicsButton);
                searchCampusLocation(academicsBttn.getText().toString(), true);
                break;
            case R.id.amenitiesButton:
                Button amenitiesBttn = findViewById(R.id.amenitiesButton);
                searchCampusLocation(amenitiesBttn.getText().toString(), true);
                break;
            case R.id.athleticsButton:
                Button athleticsBttn = findViewById(R.id.athleticsButton);
                searchCampusLocation(athleticsBttn.getText().toString(), true);
                break;
            case R.id.foodButton:
                Button foodBttn = findViewById(R.id.foodButton);
                searchCampusLocation(foodBttn.getText().toString(), true);
                break;
            case R.id.gatheringButton:
                Button gatheringsBttn = findViewById(R.id.gatheringButton);
                searchCampusLocation(gatheringsBttn.getText().toString(), true);
                break;
            case R.id.libraryButton:
                Button libraryBttn = findViewById(R.id.libraryButton);
                searchCampusLocation(libraryBttn.getText().toString(), true);
                break;
            case R.id.parkingButton:
                Button parkingBttn = findViewById(R.id.parkingButton);
                searchCampusLocation(parkingBttn.getText().toString(), true);
                break;
            case R.id.residenceButton:
                Button residenceBttn = findViewById(R.id.residenceButton);
                searchCampusLocation(residenceBttn.getText().toString(), true);
                break;
            case R.id.sacredButton:
                Button sacredBttn = findViewById(R.id.sacredButton);
                searchCampusLocation(sacredBttn.getText().toString(), true);
                break;
            case R.id.safetyButton:
                Button safetyBttn = findViewById(R.id.safetyButton);
                searchCampusLocation(safetyBttn.getText().toString(), true);
                break;
            default:
                System.out.println("Invalid button press");
        }
    }


    // UPD BUTTON METHODS-----------------------------------------------------------------------------------------------------------------------------

    // listens for button click for UPD button, initializes all global location variables to values of UPD (By Amanda Villarreal)
    public void initializeUPDButton()
    {
        Button UPDButton = findViewById(R.id.UPD);

        UPDButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // search through campus locations to find UPD
                for(CampusLocation location : campusLocationsList)
                {
                    if(location.getLocationName().contains("UPD")) // UPD found
                    {
                        // set all global variables to values of UPD location
                        selectedLocationName = location.getLocationName();
                        selectedLocationPhoneNumber = location.getPhoneNumber();
                        selectedLocationLatLng = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude()));
                        // change information bar name to UPD
                        informationBarLocationName.setText(selectedLocationName);
                        // add a marker of only UPD on map
                        stmuMap.clear();
                        stmuMap.addMarker(new MarkerOptions().position(selectedLocationLatLng).title(selectedLocationName)).showInfoWindow();
                    }
                }
            }
        });
    }


    // INFORMATION BAR METHODS------------------------------------------------------------------------------------------------------------------------

    // Change name of location on Information Bar depending on selected marker (by Amanda Villarreal)
    public void initializeMarkerListener()
    {
        stmuMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                selectedLocationName = marker.getTitle();


                // Find the selected marker's LatLng and phone number, and store the global values for later use
                for(CampusLocation location : campusLocationsList)
                {
                    if(location.getLocationName().contains(selectedLocationName))
                    {
                        selectedLocationPhoneNumber = location.getPhoneNumber();
                        selectedLocationLatLng = new LatLng(Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude()));
                    }
                }
                setInfoBarState("expand");
                informationBarLocationName.setText(selectedLocationName); // change Location Name Text View to selected marker name
                return false;
            }
        });
    }

    // This method is required by OnMarkerClickListener but is not used
    @Override
    public boolean onMarkerClick(Marker marker)
    {
        return false;
    }

    // initializes directions button in Information Bar
    public void initializeDirectionsButton()
    {
        Button directionsButton = findViewById(R.id.directButton);

        directionsButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                LatLng userLocation = new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude());
                /*
                if(userCurrentLocation == null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("Location Permissions Not Enabled");
                    builder.setMessage("Please go into your device's settings and enable location permissions");
                    builder.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
                    builder.show();
                    System.out.println("location not enabled");
                }
                else
                { */
                    // make a request for polyline
                    stmuMap.clear();
                    stmuMap.addMarker(new MarkerOptions().position(userLocation).title("You are here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
                    stmuMap.addMarker(new MarkerOptions().position(selectedLocationLatLng).title(selectedLocationName)).showInfoWindow();
                    stmuMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stMarysUniversity, 15.5f));
                    String url = getDirectionsURL(userLocation, selectedLocationLatLng, "walking");
                    new FetchURL(MapsActivity.this).execute(url, "walking");  // create a directions request
                //}
            }
        });
    }

    // initializes call button in Information Bar (By Alex Montes)
    public void initializeCallButton()
    {
        Button callButton = findViewById(R.id.callButton);

        callButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //if phone number exists
                if(!selectedLocationPhoneNumber.equals("NULL"))
                {
                    Intent call = new Intent(Intent.ACTION_DIAL);

                    //parse the telephone number ("tel:" is needed to avoided errors)
                    call.setData(Uri.parse("tel:" + selectedLocationPhoneNumber));
                    startActivity(call);
                }
                else
                {
                    AlertDialog.Builder noNumberAlert = new AlertDialog.Builder(MapsActivity.this);

                    noNumberAlert.setTitle("No Phone Number Found");
                    noNumberAlert.setMessage("This location has no phone number");
                    noNumberAlert.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
                    noNumberAlert.show();
                }

            }
        });
    }

    // initializes start button in Information Bar
    public void initializeStartButton()
    {
        Button callButton = findViewById(R.id.startButton);

        callButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // fill body here with start functionality
            }
        });
    }

    //set the state of the information bar (By Alex Montes)
    public void setInfoBarState(String state)
    {
        state = state.toLowerCase(); //just in case

        switch (state) {
            case "expand":
                informationBarBehavior.setPeekHeight(150);
                informationBarBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case "collapse":
                informationBarBehavior.setPeekHeight(0);
                informationBarBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            default:
                System.out.println("invalid state: Throw an error later");
        }
    }

    // DIRECTIONS METHODS -----------------------------------------------------------------------------------------------------------------------------

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

    // Draws polylines (by Amanda Villarreal)
    @Override
    public void onTaskDone(Object... values)
    {
        // remove existing polylines
        if(directionalPolyline != null)
            directionalPolyline.remove();

        // draw new polyline
        directionalPolyline = stmuMap.addPolyline((PolylineOptions) values[0]);

    }
}